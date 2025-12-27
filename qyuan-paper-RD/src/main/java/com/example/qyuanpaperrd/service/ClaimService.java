package com.example.qyuanpaperrd.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.qyuanpaperrd.entity.Claim;
import com.example.qyuanpaperrd.dto.ClaimRequest;
import com.example.qyuanpaperrd.common.PageResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 认领业务逻辑层接口
 */
public interface ClaimService extends IService<Claim> {

    /**
     * 提交论文认领申请
     */
    Long submitClaim(Long userId, ClaimRequest request,String paper_title);

     ArrayList<Claim> genClaims(Long userId, String last_name, String first_name, String orcid);
    /**
     * 获取用户的认领记录
     */
    PageResult<Claim> getUserClaims(Long userId, Integer status,Integer page, Integer size);


    void updateClaim(Long claim_id,Integer status,Long user_id,Long paper_id);

    PageResult<Claim> getAllClaims(Integer page, Integer size,Integer  status);
}