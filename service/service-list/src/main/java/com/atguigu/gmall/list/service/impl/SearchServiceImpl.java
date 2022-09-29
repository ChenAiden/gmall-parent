package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.repositroy.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import lombok.SneakyThrows;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Aiden
 * @create 2022-09-23 10:13
 */
@Service
@SuppressWarnings("all")
public class SearchServiceImpl implements SearchService {

    //使用repository可以启动项目自动创建索引库
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RestHighLevelClient client;


    @Override
    public void upperGoods(Long skuId) {
        //创建对象
        Goods goods = new Goods();

        //根据skuId查询skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        if (skuInfo != null) {

            goods.setId(skuInfo.getId());
            goods.setTitle(skuInfo.getSkuName());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setCreateTime(new Date());
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());


            //根据品牌id查询品牌数据对象
            Long tmId = skuInfo.getTmId();
            BaseTrademark trademark = productFeignClient.getTrademark(tmId);
            if (trademark != null) {
                goods.setTmId(tmId);
                goods.setTmName(trademark.getTmName());
                goods.setTmLogoUrl(trademark.getLogoUrl());
            }

            //根据三级分类id查询三级分类数据
            Long category3Id = skuInfo.getCategory3Id();
            BaseCategoryView categoryView = productFeignClient.getCategoryView(category3Id);
            if (categoryView != null) {
                goods.setCategory1Id(categoryView.getCategory1Id());
                goods.setCategory2Id(categoryView.getCategory2Id());
                goods.setCategory3Id(categoryView.getCategory3Id());
                goods.setCategory1Name(categoryView.getCategory1Name());
                goods.setCategory2Name(categoryView.getCategory2Name());
                goods.setCategory3Name(categoryView.getCategory3Name());
            }

            //根据平台属性id查询对应平台属性和属性值
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            if (!CollectionUtils.isEmpty(attrList)) {

                List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
                    SearchAttr searchAttr = new SearchAttr();
                    searchAttr.setAttrId(baseAttrInfo.getId());
                    searchAttr.setAttrName(baseAttrInfo.getAttrName());
                    searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
                    return searchAttr;
                }).collect(Collectors.toList());

                goods.setAttrs(searchAttrList);
            }
        }
        //添加索引
        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    /**
     * GET/api/list/inner/incrHotScore/{skuId}  更新商品的热度排名
     * <p>
     * redis 的五种数据类型
     * string
     * hash
     * list
     * set
     * zset  hotScore shkId:21 评分
     *
     * @param skuId
     */
    @Override
    public void incrHotScore(Long skuId) {
        //引入redis自增
        String hotScore = "hotScore";
        Double score = redisTemplate.opsForZSet().incrementScore(hotScore, "skuId:" + skuId, 1);

        //每10次更新一次
        if (score % 10 == 0) {
            Optional<Goods> optional = goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setId(skuId);
            goods.setHotScore(Math.round(score));
            goodsRepository.save(goods);
        }
    }


    @SneakyThrows
    @Override
    public SearchResponseVo search(SearchParam searchParam) {
        //封装拼接DSL语句
        SearchRequest request = this.builderQueryDSL(searchParam);

        //发送请求
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        //解析响应，转化数据
        SearchResponseVo searchResponseVo = this.parseSearchResponseVo(response);

        //设置每页页数
        searchResponseVo.setPageSize(searchParam.getPageSize());
        //设置当前页数
        searchResponseVo.setPageNo(searchParam.getPageNo());
        //计算总页数
        if (searchResponseVo.getTotal() % searchParam.getPageSize() == 0) {
            searchResponseVo.setTotalPages(searchResponseVo.getTotal() / searchParam.getPageSize());
        } else {
            searchResponseVo.setTotalPages(searchResponseVo.getTotal() / searchParam.getPageSize() + 1);
        }

        //方式2
        //(总记录数 + 每页页数 - 1)/每页页数

        return searchResponseVo;
    }


    //封装拼接DSL语句
    private SearchRequest builderQueryDSL(SearchParam searchParam) {
        //查询请求对象
        SearchRequest request = new SearchRequest("goods");
        //查询构造器  =  {}
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //封装多条件对象
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //查询关键字添加条件
        String keyword = searchParam.getKeyword();
        //判断
        if (!StringUtils.isEmpty(keyword)) {
            //创建条件对象
            MatchQueryBuilder title = QueryBuilders.matchQuery("title", keyword).operator(Operator.AND);
            //添加条件
            boolQuery.must(title);
        }

        //封装分类查询
        Long category1Id = searchParam.getCategory1Id();
        if (category1Id != null) {
            boolQuery.filter(QueryBuilders.termQuery("category1Id", category1Id));
        }
        Long category2Id = searchParam.getCategory2Id();
        if (category2Id != null) {
            boolQuery.filter(QueryBuilders.termQuery("category2Id", category2Id));
        }
        Long category3Id = searchParam.getCategory3Id();
        if (category3Id != null) {
            boolQuery.filter(QueryBuilders.termQuery("category3Id", category3Id));
        }

        //判断操作  2:华为
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            if (split != null && split.length == 2) {
                boolQuery.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }

        //页面提交的平台属性数组   props= 23:4G:运行内存
        //平台属性Id 平台属性值名称 平台属性名
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                if (split != null && split.length == 3) {

                    //创建多条件查询对象
                    BoolQueryBuilder boolQueryParent = QueryBuilders.boolQuery();
                    //创建子查询对象
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();

                    //封装子条件,这里使用的是must（因为是并且的关系，没有使用filter）
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));

                    //将子条件封装到 nested类型中 ，并添加到外层query请求中
                    NestedQueryBuilder attrs = QueryBuilders.nestedQuery("attrs", subBoolQuery, ScoreMode.None);//不计算得分

                    boolQueryParent.must(attrs);
                    boolQuery.filter(boolQueryParent);
                }
            }
        }
        //封装条件数据
        builder.query(boolQuery);


        //封装聚合操作,品牌聚合
        TermsAggregationBuilder tmIdAgg = AggregationBuilders.terms("tmIdAgg").field("tmId");
        tmIdAgg.subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"));
        tmIdAgg.subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));

        builder.aggregation(tmIdAgg);


        //封装聚合操作,平台聚合
        NestedAggregationBuilder nested = AggregationBuilders.nested("attrsAgg", "attrs");

        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId");
        //封装id下面的子聚合
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"));

        //将id与nested建立关系
        nested.subAggregation(attrIdAgg);

        builder.aggregation(nested);


        //排序
        // 排序规则
        // 1:hotScore 2:price
        // order = 1:asc ...
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)) {
            String[] split = order.split(":");
            if (split != null && split.length == 2) {

                String filed = "";
                switch (split[0]) {
                    case "1":
                        filed = "hotScore";
                        break;
                    case "2":
                        filed = "price";
                }

                builder.sort(filed, "asc".equals(split[1]) ? SortOrder.ASC : SortOrder.DESC);
            }
        } else {
            //没有选定排序规则时，默认按照hotScore
            builder.sort("hotScore", SortOrder.DESC);
        }


        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //添加前缀
        highlightBuilder.preTags("<font color='red'>");
        //添加前缀
        highlightBuilder.postTags("</font>");
        //字段名称
        highlightBuilder.field("title");

        builder.highlighter(highlightBuilder);


        //分页
        Integer pageNo = searchParam.getPageNo();
        Integer pageSize = searchParam.getPageSize();
        int indexStart = (pageNo - 1) * pageSize;
        builder.from(indexStart);
        builder.size(pageSize);


        //过滤返回字段
        builder.fetchSource(new String[]{
                "id",
                "title",
                "price",
                "defaultImg"}, null);

        //打印构建的DSL
        System.err.println("构建的DSL:" + "\n" + builder.toString());

        request.source(builder);
        return request;
    }


    //解析响应，转化数据
    private SearchResponseVo parseSearchResponseVo(SearchResponse response) {
        //创建返回对象
        SearchResponseVo searchResponseVo = new SearchResponseVo();

        //获取聚合信息
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();

        //获取品牌数据    key:tmIdAgg
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        List<? extends Terms.Bucket> tmBuckets = tmIdAgg.getBuckets();

        if (!CollectionUtils.isEmpty(tmBuckets)) {

            List<SearchResponseTmVo> trademarkList = tmBuckets.stream().map(bucket -> {
                SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
                //封装品牌id
                long tmId = bucket.getKeyAsNumber().longValue();
                searchResponseTmVo.setTmId(tmId);

                //获取子聚合数据
                Map<String, Aggregation> subAggregationMap = bucket.getAggregations().asMap();
                //封装品牌名字
                ParsedStringTerms tmNameAgg = (ParsedStringTerms) subAggregationMap.get("tmNameAgg");
                String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmName(tmName);
                //封装品牌图片路径
                ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) subAggregationMap.get("tmLogoUrlAgg");
                String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(tmLogoUrl);

                return searchResponseTmVo;
            }).collect(Collectors.toList());

            //封装品牌
            searchResponseVo.setTrademarkList(trademarkList);
        }


        //获取平台属性数据 key:attrsAgg
        ParsedNested attrsAgg = (ParsedNested) aggregationMap.get("attrsAgg");
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrsAgg.getAggregations().asMap().get("attrIdAgg");

        List<? extends Terms.Bucket> attrIdBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(attrIdBuckets)) {

            List<SearchResponseAttrVo> attrsList = attrIdBuckets.stream().map(bucket -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                //设置平台属性id
                long attrId = bucket.getKeyAsNumber().longValue();
                searchResponseAttrVo.setAttrId(attrId);
                //获取聚合子集
                Map<String, Aggregation> subAggregationMap = bucket.getAggregations().asMap();
                //设置平台属性名称
                ParsedStringTerms attrNameAgg = (ParsedStringTerms) subAggregationMap.get("attrNameAgg");
                String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);
                //设置平台属性值集合
                ParsedStringTerms attrValueAgg = (ParsedStringTerms) subAggregationMap.get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(attrValueAggBuckets)) {
                    List<String> attrValueList = attrValueAggBuckets.stream()
                            .map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());

                    searchResponseAttrVo.setAttrValueList(attrValueList);
                }
                return searchResponseAttrVo;
            }).collect(Collectors.toList());

            searchResponseVo.setAttrsList(attrsList);
        }


        //封装商品goods
        SearchHits searchHits = response.getHits();
        SearchHit[] hits = searchHits.getHits();

        if (hits != null && hits.length > 0) {
            List<Goods> goodsList = Arrays.stream(hits).map(hit -> {
                String sourceAsString = hit.getSourceAsString();
                //转换成goods对象
                Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);

                //替换成高亮数据
                //判断,是否有高亮数据，避免title为null
                if (hit.getHighlightFields().get("title") != null) {
                    String title = hit.getHighlightFields().get("title").getFragments()[0].toString();
                    goods.setTitle(title);
                }

                return goods;
            }).collect(Collectors.toList());

            //设置商品集合
            searchResponseVo.setGoodsList(goodsList);
        }

        //获取总记录数
        long total = searchHits.getTotalHits().value;
        searchResponseVo.setTotal(total);

        return searchResponseVo;
    }


}
