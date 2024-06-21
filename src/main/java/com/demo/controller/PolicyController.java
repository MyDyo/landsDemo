package com.demo.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.entity.Policy;
import com.demo.mapper.PolicyMapper;
import com.demo.service.PolicyService;
import com.demo.util.resultCode.R;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private Policy policy;
    @Autowired
    private PolicyService policyService;

    @GetMapping("getPolicy/{policyId}")
    public R getPolicy(@PathVariable Integer policyId){
        policy =policyService.getById(policyId);
        String title= policy.getTitle();
        String content = policy.getContent();
        String source = policy.getSource();
        Date uploadTime = policy.getUploadTime();
        return R.ok().data("title",title).data("content",content).data("source",source).data("uploadTime",uploadTime);
    }


//    @GetMapping("/viewPolicy")
//    public List<Policy> getAllPolicies(@RequestParam(defaultValue = "1") int page,
//                                       @RequestParam(defaultValue = "10") int pageSize) {
//        return policyService.findAll(page, pageSize);
//    }
//
//    @GetMapping("/search")
//    public List<Policy> searchPolicies(@RequestParam String keyword,
//                                       @RequestParam(defaultValue = "1") int page,
//                                       @RequestParam(defaultValue = "10") int pageSize) {
//        return policyService.findByTitleContaining(keyword, page, pageSize);
//    }

}
//    //构建政策列表
//    @GetMapping("viewPolicy")
//    public R getAllPolicies(@RequestParam(defaultValue = "1") Integer page,
//                                           @RequestParam(defaultValue = "10") Integer size,){
//        List<Policy> policies = policyService.getPolicies(page, size,null);
//        return R.ok().data("policies", policies);
//    }
//
//
//    // 搜索政策
//    @GetMapping("/search")
//    public R searchPolicies(@RequestParam Integer page,
//                                            @RequestParam Integer size,
//                                            @RequestParam String keyword) {
//        List<Policy> policies = policyService.getPolicies(page, size,keyword);
//        return R.ok().data("policies", policies);
//    }
//}
//
//    @GetMapping("/search")
//    public R searchTitle(@RequestParam String keyword) {
//        List<Policy> policies = policyMapper.findByTitleContaining(keyword);
//        return R.ok().data("policies",policies);
//    }
//
////    等于按条件查询的分页
//    private R getPolicies(Integer page, Integer size, String keyword) {
//        Page<Policy> policyPage = new Page<>(page, size);
//        policyService.findPolicies(policyPage, keyword);
//        long total = policyPage.getTotal();
//        List<Policy> policies = policyPage.getRecords();
//        return R.ok().data("total",total).data("policies",policies);
//    }
//}

//    //条件查询带分页
//    @PostMapping("pagePolicyFind/{current}/{limit}/{keywords}")
//    public R pageOrderListFind(long current,long limit,String keywords){
//        //创建page对象，传递当前页和每页记录数
//        Page<Policy> pagePolicy =new Page<>(current,limit);
//        QueryWrapper<Policy> wrapper=new QueryWrapper<>();
//        //wrapper构建条件,多条件组合查询 动态sql，判断条件是否为空，如果不为空拼接条件
//        if (!StringUtils.isEmpty(keywords)){
//            wrapper.like("title", keywords);
//        }
//        policyService.page(pagePolicy,wrapper);
//        long total = pagePolicy.getTotal();   //总记录数
//        List<Policy> records = pagePolicy.getRecords(); //数据list集合
//        return R.ok().data("total",total).data("rows",records);
//    }
