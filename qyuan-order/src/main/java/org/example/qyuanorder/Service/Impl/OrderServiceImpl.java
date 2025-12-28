package org.example.qyuanorder.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.qyuanorder.Entity.Order;
import org.example.qyuanorder.Feign.UserSecurityFeign;
import org.example.qyuanorder.Mapper.OrderMapper;
import org.example.qyuanorder.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/22 15:07
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper,Order>implements OrderService {
    @Resource
    private OrderMapper orderMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Resource
    private UserSecurityFeign userSecurityFeign;
    String orderKey="123456789123456789123456789";
    @Override
    public Page<Order> getMyOrders(Integer user_id, Integer pageNum, Integer pageSize) {
        // 构建Redis key
        String redisKey = "orders:user:" + user_id + ":page:" + pageNum + ":size:" + pageSize;

        // 尝试从Redis获取缓存
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        Object cacheResult = operations.get(redisKey);
        if (cacheResult != null) {
            System.out.println("从Redis获取订单数据");
            return (Page<Order>) cacheResult;
        }

        // 缓存未命中，查询数据库
        System.out.println("从数据库获取订单数据");
        Page<Order> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", user_id)
                .orderByDesc("create_at"); // 按创建时间倒序
        Page<Order> result = this.page(page, wrapper);

        // 将结果存入Redis，设置过期时间为5分钟
        operations.set(redisKey, result, 300, TimeUnit.SECONDS);
        System.out.println("将订单数据存入Redis");

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

        // 清除该用户相关的缓存
        clearUserOrderCache(userId);

        return order;
    }

    @Override
    public Order payOrder(int user_id,int orderId, BigDecimal payAccount, String payMethod) {
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
        Integer buyDay=order.getBuyDay();
        LocalDateTime expire_time=LocalDateTime.now().plusDays(buyDay);
        //TODO:实现用户侧接口
        userSecurityFeign.payForVIP(user_id,expire_time, orderKey);
        // 发送Kafka消息

        Map<String, String> message = new HashMap<>();
        message.put("title", "支付成功");
        String content = String.format("尊敬的会员,您好! 您购买的时长为%s天的会员已生效，会员有效期至%s", buyDay, expire_time);
        message.put("content", content);
        message.put("userId", String.valueOf(user_id));
        kafkaTemplate.send("message-buy-vip-topic", message);

        // 清除该用户相关的缓存
        clearUserOrderCache(user_id);

        return order;
    }

    /**
     * 清除用户订单相关的缓存
     * @param userId 用户ID
     */
    private void clearUserOrderCache(Integer userId) {
        // 实际应用中，可能需要通过扫描或维护key列表的方式来删除所有相关缓存
        // 这里为了简化示例，只展示思路
        String pattern = "orders:user:" + userId + ":*";
        // 注意：生产环境中可能需要更精确地清理缓存，这里简化处理
        System.out.println("清除用户" + userId + "的订单缓存，pattern: " + pattern);

    }
}