package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author Aiden
 * @create 2022-09-26 15:23
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {

    //spring 的 判断路径是否满足 xx 条件的工具类
    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${authUrls.url}")
    private String authUrls;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        //获取请求路径
        String path = request.getURI().getPath();

        //判断是否为内部接口
        //api/admin/inner/product   /**/inner/**
        //antPathMatcher.match(pattern,path);可以判断path是否满足pattern（正则条件）
        if (antPathMatcher.match("/**/inner/**", path)) {
            //满足条件拒绝访问
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }

        //获取userId，判断用户是否登陆
        String userId = getUserId(request);

        if ("-1".equals(userId)) {
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.ILLEGAL_REQUEST);
        }

        //过滤auth路径   此路径必须登陆才可以访问
        if (antPathMatcher.match("/**/auth/**", path)) {
            //判断
            if (StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                //过滤auth路径  未登录的直接报错，不重定向，直接报错
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }

        //白名单 白名单中的路径必须登陆才可以访问
        String[] split = authUrls.split(",");
        for (String url : split) {
            if (path.contains(url) && StringUtils.isEmpty(userId)) {
                //路径正确，并且未登陆   重定向登陆
                ServerHttpResponse response = exchange.getResponse();
                //设置重定向
                response.setStatusCode(HttpStatus.SEE_OTHER);//设定303，告知浏览器以get请求重定向
                //设置重定向页面   转到登陆页面，并将本页面的路径附到后面，用以再跳转回来
                response.getHeaders().set(HttpHeaders.LOCATION, "http://www.gmall.com/login.html?originUrl=" + request.getURI());
            }
        }

        //存储userId，以后用
        if (!StringUtils.isEmpty(userId)) {
            request.mutate().header("userId", userId).build();
            return chain.filter(exchange.mutate().request(request).build());
        }

        return chain.filter(exchange);
    }

    /**
     * 如果userid不存在说明用户未登陆
     * <p>
     * token可能在  1.header  2.cookie中携带
     * <p>
     * 结果定义
     * 1.没有token  没有定义
     * 2.有token
     *      有值  返回userId
     *       无值 ip不对
     *
     * @param request
     * @return
     */
    private String getUserId(ServerHttpRequest request) {
        //从头信息中获取token
        String token = request.getHeaders().getFirst("token");
        //判断如果不存在，从cookie中获取
        if (StringUtils.isEmpty(token)) {
            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
            if (cookies != null) {

                HttpCookie cookieToken = cookies.getFirst("token");
                if (cookieToken != null) {

                    token = cookieToken.getValue();
                }
            }
        }
        //获取了token，判断一下token
        if (!StringUtils.isEmpty(token)) {
            //从redis中获取
            String strJon = (String) redisTemplate.opsForValue().get("user:login" + token);

            if (!StringUtils.isEmpty(strJon)) {
                //转换成JSONObject
                JSONObject jsonObject = JSONObject.parseObject(strJon);

                //获取userId
                String userId = (String) jsonObject.get("userId");
                String ip = (String) jsonObject.get("ip");
                //判断
                if (!StringUtils.isEmpty(userId)) {
                    //获取ip
                    String gatwayIpAddress = IpUtil.getGatwayIpAddress(request);
                    if (gatwayIpAddress.equals(ip)) {
                        //ip正确返回userId
                        return userId;
                    } else {
                        //说明不是登陆时的ip
                        return "-1";
                    }
                }
            }
        }

        //request请求中没有userId，用户还没有登陆
        return "";
    }

    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum permission) {
        //封装响应信息
        Result<Object> result = Result.build(null, permission);

        //创建缓冲区DataBuffer
        DataBuffer wrap = response.bufferFactory().wrap(JSON.toJSONString(result).getBytes());

        //中文处理
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        return response.writeWith(Mono.just(wrap));
    }
}
