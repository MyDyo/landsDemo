package com.demo.service.Impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.demo.entity.Policy;
import com.demo.mapper.PolicyMapper;
import com.demo.service.PolicyService;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.List;

@Service
public class PolicyServiceImpl extends ServiceImpl<PolicyMapper,Policy> implements PolicyService {
//    @Autowired
//    private PolicyMapper policyMapper;
//
//    public List<Policy> findAll(int page, int pageSize) {
//        return policyMapper.findAll(new RowBounds((page - 1) * pageSize, pageSize));
//    }
//
//    public List<Policy> findByTitleContaining(String title, int page, int pageSize) {
//        return policyMapper.findByTitleContaining(title, new RowBounds((page - 1) * pageSize, pageSize));
//    }
}
