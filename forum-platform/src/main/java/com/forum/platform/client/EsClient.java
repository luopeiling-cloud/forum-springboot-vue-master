package com.forum.platform.client;

import com.forum.xo.vo.NoteSearchVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author xiaozhao
 */
@FeignClient(value = "forum-search")
@Component
public interface EsClient {

    @RequestMapping("/search/note/test")
    void test() ;

    /**
     * 增加一条笔记
     * @param noteSearchVo 笔记实体
     */
    @RequestMapping("/search/note/addNote")
    void addNote(@RequestBody NoteSearchVo noteSearchVo);

    /**
     * 批量增加笔记
     * @param noteSearchVoList 笔记集合
     */
    @RequestMapping("/search/note/addNoteBulkData")
    void addNoteBulkData(@RequestBody List<NoteSearchVo> noteSearchVoList);
}
