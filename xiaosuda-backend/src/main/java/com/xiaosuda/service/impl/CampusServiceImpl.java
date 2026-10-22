package com.xiaosuda.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaosuda.entity.CampusPoint;
import com.xiaosuda.mapper.CampusPointMapper;
import com.xiaosuda.service.CampusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CampusServiceImpl implements CampusService {

    @Autowired
    private CampusPointMapper campusPointMapper;

    @Override
    public List<CampusPoint> list() {
        return campusPointMapper.selectList(
                new LambdaQueryWrapper<CampusPoint>()
                        .orderByAsc(CampusPoint::getSortOrder));
    }
}
