package com.atguigu.gmall.weball.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Aiden
 * @create 2022-09-24 15:14
 */
@Controller
@SuppressWarnings("all")
public class ListController {

    @Autowired
    private ListFeignClient listFeignClient;

    @ApiOperation("搜索页面")
    @GetMapping("/list.html")
    public String list(SearchParam searchParam, Model model) {
        //接收的是json,我们可以使用Map接收
        Result<Map> search = listFeignClient.search(searchParam);

        model.addAllAttributes(search.getData());

        //请求参数回显
        model.addAttribute("searchParam", searchParam);

        //拼接路径
        String urlParam = this.makeUrlParam(searchParam);
        model.addAttribute("urlParam", urlParam);
        //添加品牌面包屑
        String trademarkParam = this.makeTrademarkParam(searchParam.getTrademark());
        model.addAttribute("trademarkParam", trademarkParam);
        //添加品牌面包屑
        List<SearchAttr> propsParamList = this.makeProps(searchParam.getProps());
        model.addAttribute("propsParamList", propsParamList);
        //排序
        Map<String, String> orderMap = this.dealOrder(searchParam.getOrder());
        model.addAttribute("orderMap", orderMap);

        return "list/index";
    }


    /**
     * 拼接搜索路径
     *
     * @param searchParam
     * @return
     */
    private String makeUrlParam(SearchParam searchParam) {

        StringBuilder stringBuilder = new StringBuilder();
        //封装关键字
        if (searchParam.getKeyword() != null) {//这里设置只检测不为null可以为“”，这样直接搜索出所有的商品
            stringBuilder.append("keyword=").append(searchParam.getKeyword());
        }
        //封装三级分类id
        if (searchParam.getCategory1Id() != null) {
            stringBuilder.append("category1Id=").append(searchParam.getCategory1Id());
        }
        if (searchParam.getCategory2Id() != null) {
            stringBuilder.append("category2Id=").append(searchParam.getCategory2Id());
        }
        if (searchParam.getCategory3Id() != null) {
            stringBuilder.append("category3Id=").append(searchParam.getCategory3Id());
        }
        //拼接品牌
        //&tradmark=1:小米
        if (stringBuilder.length() > 0) {
            if (searchParam.getTrademark() != null) {
                stringBuilder.append("&trademark=").append(searchParam.getTrademark());
            }

            //拼接平台属性
            String[] props = searchParam.getProps();
            if (props != null && props.length > 0) {
                for (String prop : props) {
                    stringBuilder.append("&props=").append(prop);
                }
            }
        }

        return "list.html?" + stringBuilder.toString();
    }


    /**
     * 添加品牌面包屑
     *
     * @param trademark
     * @return
     */
    private String makeTrademarkParam(String trademark) {
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            if (split != null && split.length == 2) {
                return "品牌：" + split[1];
            }
        }
        return "";
    }

    /**
     * 添加平台属性面包屑
     *
     * @param props
     * @return
     */
    private List<SearchAttr> makeProps(String[] props) {
        //props=23:4G:运行内存
        List<SearchAttr> propsParamList = new ArrayList<>();
        if (props != null && props.length != 0) {
            for (String prop : props) {
                SearchAttr searchAttr = new SearchAttr();
                String[] split = prop.split(":");
                searchAttr.setAttrId(Long.parseLong(split[0]));
                searchAttr.setAttrName(split[2]);
                searchAttr.setAttrValue(split[1]);
                propsParamList.add(searchAttr);
            }
        }
        return propsParamList;
    }

    /**
     * 排序
     *
     * @param order
     * @return
     */
    private Map<String, String> dealOrder(String order) {
        Map<String, String> orderMap = new HashMap<>();
        //1:hotScore
        if (!StringUtils.isEmpty(order)) {

            String[] split = order.split(":");

            if (split != null && split.length == 2) {
                orderMap.put("type", split[0]);
                orderMap.put("sort", split[1]);
            }
        } else {
            orderMap.put("type", "1");
            orderMap.put("sort", "asc");
        }
        return orderMap;
    }

}
