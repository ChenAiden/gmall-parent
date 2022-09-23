package com.atguigu.gmall.list.repositroy;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Aiden
 * @create 2022-09-23 10:15
 */
@Repository
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {

}
