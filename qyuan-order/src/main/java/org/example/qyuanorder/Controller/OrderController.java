package org.example.qyuanorder.Controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.example.qyuancommon.Result;

import org.example.qyuanorder.Entity.Order;

import org.example.qyuanorder.Service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @author: 29177
 * @time: 2025/11/22 15:04
 */
@RequestMapping("/order")
@RestController
public class OrderController {
    @Resource
    private OrderService orderService;

    @GetMapping("/getMyOrders")
    public Result<Object> getMyOrders(@RequestHeader(value = "USER-ID") int user_id,
    @RequestParam("page_num") int pageNum,@RequestParam("page_size") int pageSize){
        try{
            Page<Order> myOrders = orderService.getMyOrders(user_id,pageNum,pageSize);
            return Result.ok(myOrders);
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/createOrder")
    public Result<Object> createOrder(@RequestHeader(value = "USER-ID") int user_id,
                                      @RequestBody JSONObject body){
        try {
            int buy_day=body.getInteger("buyDay");
            Order order=orderService.createOrder(user_id,buy_day);
            return Result.ok(order);
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }

    @PostMapping("/payOrder/{order_id}")
    public Result<Object> payOrder(@PathVariable("order_id") int order_id,
                                   @RequestBody JSONObject body){
        try {
            BigDecimal payAccount=body.getBigDecimal("payAccount");
            String payMethor=body.getString("payMethod");
            Order order=orderService.payOrder(order_id,payAccount,payMethor);
            return Result.ok(order);
        }catch (Exception  e){
            return Result.fail(e.getMessage());
        }
    }

}
