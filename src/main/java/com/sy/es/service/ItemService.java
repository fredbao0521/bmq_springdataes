package com.sy.es.service;

import com.sy.es.model.Item;
import com.sy.es.repository.ItemRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {
    /**
     * 操作方式1
     */
    @Autowired
    private ElasticsearchRestTemplate template;
    /**
     * 操作方式2
     */
    @Autowired
    private ItemRepository repository;

    public boolean addIndex(){
        boolean index = template.createIndex(Item.class);
        template.putMapping(Item.class);
        return index;
    }

    /**
     * 添加索引文档
     * @param item
     * @return
     */
    public String addDoc(Item item){
        IndexQuery indexQuery = new IndexQueryBuilder().withId(item.getId().toString()).withObject(item).build();
        String index = template.index(indexQuery);
        return index;
    }

    public List<Item> matchQuery(String title){
        //构建查询器,按什么方式查询
        MatchQueryBuilder title1 = QueryBuilders.matchQuery("title", title);
        //构建检索器,封装查询器,构建出DSL查询体
        NativeSearchQuery build = new NativeSearchQueryBuilder().withQuery(title1).build();
        List<Item> items = template.queryForList(build, Item.class);
        return items;
    }

    /**
     * 带分页和排序的全文检索
     * @param title
     * @return
     */
    public List<Item> findPageAndSort(String title){
        //构建查询器,按什么方式查询
        MatchQueryBuilder title1 = QueryBuilders.matchQuery("title", title);
        //构建分页查询器
        Pageable pageable = PageRequest.of(0,5);
        //构建排序查询器
        ScoreSortBuilder order = SortBuilders.scoreSort().order(SortOrder.DESC);

        FieldSortBuilder price = SortBuilders.fieldSort("price").order(SortOrder.ASC);
        //构建检索器,封装查询器,构建出DSL查询体
        NativeSearchQuery build = new NativeSearchQueryBuilder()
                .withQuery(title1)
                .withSort(order)
                .withPageable(pageable)
                .build();
        List<Item> items = template.queryForList(build, Item.class);
        return items;
    }

    /**
     * templdate 高亮检索
     * 注意:使用模板高亮检索较为复杂,最好转到RestClient客户端操作
     *
     * @return
     */
    public AggregatedPage<Item> findWithHighLight() {

        SearchQuery query = new NativeSearchQueryBuilder()
                //查询条件
                .withQuery(QueryBuilders.matchQuery("title", "小米"))
                        //高亮检索
                        .withHighlightBuilder( new HighlightBuilder().field("title")
                                .preTags("<h1>").postTags("</h1>"))
                        .build();

        //注意:高亮检索是一种聚合查询行为,所以要用queryForPage()方法,需自定义聚合结果映射器
        //List<User> list = template.queryForList(query, User.class);
        AggregatedPage<Item> itemPage = template.queryForPage(query, Item.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse,
                                                    Class<T> aClass, Pageable pageable) {
                //1.searchResponse:文档返回数据
                //2.获取SearchHit中数据
                //3.调用mapSearchHit方法封装数据
                SearchHits searchHits = searchResponse.getHits();
                SearchHit[] hits = searchHits.getHits();
                List<T> list = new ArrayList<>();
                for (SearchHit hit : hits) {
                    T t = mapSearchHit(hit, aClass);
                    list.add(t);
                }
                AggregatedPage page = new AggregatedPageImpl(list);
                return page;
            }

            @Override
            public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                //解析searchHit,获取高亮数据,映射到适当的T类型对像中
                return null;
            }
        });

        return itemPage;
    }
}
