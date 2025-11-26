package org.example.qyuanorder.Service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.qyuancommon.Result;
import org.example.qyuanorder.Entity.Order;


import java.math.BigDecimal;
import java.util.List;

public interface OrderService {
    Page<Order> getMyOrders(Integer user_id, Integer pageNum, Integer pageSize);

    Order createOrder(int userId, int buyDay);

    Order payOrder(int orderId, BigDecimal payAccount, String payMethor);
}
