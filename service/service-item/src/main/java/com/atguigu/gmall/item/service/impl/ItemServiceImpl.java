package com.atguigu.gmall.item.service.impl;

import com.atguigu.gmall.item.service.ItemService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Aiden
 * @create 2022-09-15 22:21
 */
@Service
public class ItemServiceImpl implements ItemService {


//    @Autowired
//    private ProductFeignClient productFeignClient;

    @Override
    public Map<String, Object> getBySkuInfo(Long skuId) {

        Map<String, Object> resultMap = new HashMap<>();
//
//        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
//        BigDecimal price = productFeignClient.getSkuPrice(skuId);
//
//        Long spuId = skuInfo.getSpuId();
//        List<SpuPoster> spuPosterList = productFeignClient.findSpuPosterBySpuId(spuId);



        return resultMap;
    }
}
