package com.demo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.demo.entity.Policy;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PolicyService extends IService<Policy> {
//    List<Policy> listPolicies();
//    void findPolicies(Page<Policy> page, String keyword);
//    public List<Policy> findAll(int page, int pageSize) ;
//    public List<Policy> findByTitleContaining(String title, int page, int pageSize) ;
}