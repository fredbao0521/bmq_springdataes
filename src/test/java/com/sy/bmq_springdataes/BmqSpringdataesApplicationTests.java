package com.sy.bmq_springdataes;

import com.sy.BmqSpringdataesApplication;
import com.sy.es.model.Item;
import com.sy.es.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = BmqSpringdataesApplication.class)
class BmqSpringdataesApplicationTests {
    @Autowired
    private ItemService service;

    @Test
    public void creatIndex() {
        boolean b = service.addIndex();
        System.out.println("创建索引成功了么?"+b);
    }

    @Test
    public void addDoc(){
        Item item = new Item(1L, "小米", "手机", "小米", 2000.0, "http://baidu.com");
        String s = service.addDoc(item);
        System.out.println(s);
    }

    @Test
    public void matchQuery(){
        List<Item> 手机 = service.matchQuery("小米");
        System.out.println(手机);
    }

    /**
     * 带分页和排序的全文检索
     * @param
     * @return
     */
    @Test
    public void sortAndPageQuery(){
        List<Item> list = service.findPageAndSort("小米");
        System.out.println(list);
    }
}
