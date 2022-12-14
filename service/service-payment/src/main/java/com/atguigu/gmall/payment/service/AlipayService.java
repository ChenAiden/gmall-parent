package com.atguigu.gmall.payment.service;

/**
 * @author Aiden
 * @create 2022-10-04 16:28
 */
public interface AlipayService {

    String submit(Long orderId);

    boolean refund(Long orderId);

    boolean checkPayment(Long orderId);

    boolean closePay(Long orderId);

}
