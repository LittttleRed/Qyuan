package org.example.qyuanorder.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.qyuanorder.Entity.Order;
import org.example.qyuanorder.Mapper.OrderMapper;
import org.example.qyuanorder.Service.OrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/22 15:07
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper,Order>implements OrderService {
    @Resource
    private OrderMapper orderMapper;


    @Override
    public Page<Order> getMyOrders(Integer user_id, Integer pageNum, Integer pageSize) {
        System.out.println(user_id);
        Page<Order> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", user_id)
                .orderByDesc("create_at"); // 按创建时间倒序
        Page<Order> result = this.page(page, wrapper);
        return result;
    }

    @Override
    public Order createOrder(int userId, int buyDay) {
        Order order=new Order();
        order.setUserId(userId);
        order.setBuyDay(buyDay);
        order.setPayStatus(0);
        order.setCreateAt(LocalDateTime.now());
        this.save(order);
        return order;
    }

    @Override
    public Order payOrder(int orderId, BigDecimal payAccount, String payMethod) {
        // 1. 根据orderId查询订单
        Order order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在，orderId: " + orderId);
        }
        // 2. 更新订单支付信息
        order.setPayAccount(payAccount);
        order.setPayMethod(payMethod); // 修正拼写错误
        order.setPayStatus(1); // 假设1表示已支付
        order.setPayTime(LocalDateTime.now());
        // 3. 更新订单（使用updateById而不是save）
        this.updateById(order);
        return order;
    }

}

