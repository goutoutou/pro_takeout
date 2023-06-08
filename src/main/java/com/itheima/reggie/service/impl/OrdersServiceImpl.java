package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private UserService userService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;

    @Override
    @Transactional
    public void submit(Orders orders) {
        //获得当前用户id
        Long userId = BaseContext.getCurrentId();
        //查询当前用户购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        if (shoppingCarts==null || shoppingCarts.size()==0){
            throw new CustomException("购物车为空，不能下单");
        }
        //查用户表和地址表
        //用户
        User user = userService.getById(userId);
        //地址
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook==null){
            throw  new CustomException("地址信息有误，不能下单");
        }
        long orderId = IdWorker.getId();//idwordker类用于产生唯一的id
        /**
         * 遍历购物车数据，计算总金额,封装订单明细
         */
        AtomicInteger amount = new AtomicInteger(0);
        List<OrderDetail> orderDetails= shoppingCarts.stream().map((item)->{
              OrderDetail orderDetail = new OrderDetail();
              orderDetail.setOrderId(orderId);
              orderDetail.setNumber(item.getNumber());
              orderDetail.setDishFlavor(item.getDishFlavor());
              orderDetail.setDishId(item.getDishId());
              orderDetail.setSetmealId(item.getSetmealId());
              orderDetail.setName(item.getName());
              orderDetail.setImage(item.getImage());
              orderDetail.setAmount(item.getAmount());
              amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
              return orderDetail;
        }).collect(Collectors.toList());
        //向订单表插入数据，一条数据
        orders.setNumber(String.valueOf(orderId));
        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);//待派送
        orders.setUserId(userId);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());//收货人
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName()==null? "":addressBook.getProvinceName())+
                (addressBook.getCityName()==null ? "": addressBook.getCityName())+
                (addressBook.getDistrictName()==null ? "" : addressBook.getDistrictName())+
                (addressBook.getDetail()==null ? "" : addressBook.getDetail()));
        //订单表插入一条数据
        this.save(orders);
        //向订单明细表插入数据多条
        orderDetailService.saveBatch(orderDetails);
        //清空购物车数据
        shoppingCartService.remove(queryWrapper);
    }
}
