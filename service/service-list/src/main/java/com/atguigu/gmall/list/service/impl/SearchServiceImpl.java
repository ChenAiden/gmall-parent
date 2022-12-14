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

    //??????repository???????????????????????????????????????
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
        //????????????
        Goods goods = new Goods();

        //??????skuId??????skuInfo
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        if (skuInfo != null) {

            goods.setId(skuInfo.getId());
            goods.setTitle(skuInfo.getSkuName());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setCreateTime(new Date());
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());


            //????????????id????????????????????????
            Long tmId = skuInfo.getTmId();
            BaseTrademark trademark = productFeignClient.getTrademark(tmId);
            if (trademark != null) {
                goods.setTmId(tmId);
                goods.setTmName(trademark.getTmName());
                goods.setTmLogoUrl(trademark.getLogoUrl());
            }

            //??????????????????id????????????????????????
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

            //??????????????????id????????????????????????????????????
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
        //????????????
        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }

    /**
     * GET/api/list/inner/incrHotScore/{skuId}  ???????????????????????????
     * <p>
     * redis ?????????????????????
     * string
     * hash
     * list
     * set
     * zset  hotScore shkId:21 ??????
     *
     * @param skuId
     */
    @Override
    public void incrHotScore(Long skuId) {
        //??????redis??????
        String hotScore = "hotScore";
        Double score = redisTemplate.opsForZSet().incrementScore(hotScore, "skuId:" + skuId, 1);

        //???10???????????????
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
        //????????????DSL??????
        SearchRequest request = this.builderQueryDSL(searchParam);

        //????????????
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);

        //???????????????????????????
        SearchResponseVo searchResponseVo = this.parseSearchResponseVo(response);

        //??????????????????
        searchResponseVo.setPageSize(searchParam.getPageSize());
        //??????????????????
        searchResponseVo.setPageNo(searchParam.getPageNo());
        //???????????????
        if (searchResponseVo.getTotal() % searchParam.getPageSize() == 0) {
            searchResponseVo.setTotalPages(searchResponseVo.getTotal() / searchParam.getPageSize());
        } else {
            searchResponseVo.setTotalPages(searchResponseVo.getTotal() / searchParam.getPageSize() + 1);
        }

        //??????2
        //(???????????? + ???????????? - 1)/????????????

        return searchResponseVo;
    }


    //????????????DSL??????
    private SearchRequest builderQueryDSL(SearchParam searchParam) {
        //??????????????????
        SearchRequest request = new SearchRequest("goods");
        //???????????????  =  {}
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //?????????????????????
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        //???????????????????????????
        String keyword = searchParam.getKeyword();
        //??????
        if (!StringUtils.isEmpty(keyword)) {
            //??????????????????
            MatchQueryBuilder title = QueryBuilders.matchQuery("title", keyword).operator(Operator.AND);
            //????????????
            boolQuery.must(title);
        }

        //??????????????????
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

        //????????????  2:??????
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)) {
            String[] split = trademark.split(":");
            if (split != null && split.length == 2) {
                boolQuery.filter(QueryBuilders.termQuery("tmId", split[0]));
            }
        }

        //?????????????????????????????????   props= 23:4G:????????????
        //????????????Id ????????????????????? ???????????????
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                if (split != null && split.length == 3) {

                    //???????????????????????????
                    BoolQueryBuilder boolQueryParent = QueryBuilders.boolQuery();
                    //?????????????????????
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();

                    //???????????????,??????????????????must??????????????????????????????????????????filter???
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", split[1]));

                    //????????????????????? nested????????? ?????????????????????query?????????
                    NestedQueryBuilder attrs = QueryBuilders.nestedQuery("attrs", subBoolQuery, ScoreMode.None);//???????????????

                    boolQueryParent.must(attrs);
                    boolQuery.filter(boolQueryParent);
                }
            }
        }
        //??????????????????
        builder.query(boolQuery);


        //??????????????????,????????????
        TermsAggregationBuilder tmIdAgg = AggregationBuilders.terms("tmIdAgg").field("tmId");
        tmIdAgg.subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"));
        tmIdAgg.subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));

        builder.aggregation(tmIdAgg);


        //??????????????????,????????????
        NestedAggregationBuilder nested = AggregationBuilders.nested("attrsAgg", "attrs");

        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId");
        //??????id??????????????????
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"));

        //???id???nested????????????
        nested.subAggregation(attrIdAgg);

        builder.aggregation(nested);


        //??????
        // ????????????
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
            //??????????????????????????????????????????hotScore
            builder.sort("hotScore", SortOrder.DESC);
        }


        //????????????
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //????????????
        highlightBuilder.preTags("<font color='red'>");
        //????????????
        highlightBuilder.postTags("</font>");
        //????????????
        highlightBuilder.field("title");

        builder.highlighter(highlightBuilder);


        //??????
        Integer pageNo = searchParam.getPageNo();
        Integer pageSize = searchParam.getPageSize();
        int indexStart = (pageNo - 1) * pageSize;
        builder.from(indexStart);
        builder.size(pageSize);


        //??????????????????
        builder.fetchSource(new String[]{
                "id",
                "title",
                "price",
                "defaultImg"}, null);

        //???????????????DSL
        System.err.println("?????????DSL:" + "\n" + builder.toString());

        request.source(builder);
        return request;
    }


    //???????????????????????????
    private SearchResponseVo parseSearchResponseVo(SearchResponse response) {
        //??????????????????
        SearchResponseVo searchResponseVo = new SearchResponseVo();

        //??????????????????
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();

        //??????????????????    key:tmIdAgg
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        List<? extends Terms.Bucket> tmBuckets = tmIdAgg.getBuckets();

        if (!CollectionUtils.isEmpty(tmBuckets)) {

            List<SearchResponseTmVo> trademarkList = tmBuckets.stream().map(bucket -> {
                SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
                //????????????id
                long tmId = bucket.getKeyAsNumber().longValue();
                searchResponseTmVo.setTmId(tmId);

                //?????????????????????
                Map<String, Aggregation> subAggregationMap = bucket.getAggregations().asMap();
                //??????????????????
                ParsedStringTerms tmNameAgg = (ParsedStringTerms) subAggregationMap.get("tmNameAgg");
                String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmName(tmName);
                //????????????????????????
                ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) subAggregationMap.get("tmLogoUrlAgg");
                String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(tmLogoUrl);

                return searchResponseTmVo;
            }).collect(Collectors.toList());

            //????????????
            searchResponseVo.setTrademarkList(trademarkList);
        }


        //???????????????????????? key:attrsAgg
        ParsedNested attrsAgg = (ParsedNested) aggregationMap.get("attrsAgg");
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrsAgg.getAggregations().asMap().get("attrIdAgg");

        List<? extends Terms.Bucket> attrIdBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(attrIdBuckets)) {

            List<SearchResponseAttrVo> attrsList = attrIdBuckets.stream().map(bucket -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                //??????????????????id
                long attrId = bucket.getKeyAsNumber().longValue();
                searchResponseAttrVo.setAttrId(attrId);
                //??????????????????
                Map<String, Aggregation> subAggregationMap = bucket.getAggregations().asMap();
                //????????????????????????
                ParsedStringTerms attrNameAgg = (ParsedStringTerms) subAggregationMap.get("attrNameAgg");
                String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);
                //???????????????????????????
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


        //????????????goods
        SearchHits searchHits = response.getHits();
        SearchHit[] hits = searchHits.getHits();

        if (hits != null && hits.length > 0) {
            List<Goods> goodsList = Arrays.stream(hits).map(hit -> {
                String sourceAsString = hit.getSourceAsString();
                //?????????goods??????
                Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);

                //?????????????????????
                //??????,??????????????????????????????title???null
                if (hit.getHighlightFields().get("title") != null) {
                    String title = hit.getHighlightFields().get("title").getFragments()[0].toString();
                    goods.setTitle(title);
                }

                return goods;
            }).collect(Collectors.toList());

            //??????????????????
            searchResponseVo.setGoodsList(goodsList);
        }

        //??????????????????
        long total = searchHits.getTotalHits().value;
        searchResponseVo.setTotal(total);

        return searchResponseVo;
    }


}
