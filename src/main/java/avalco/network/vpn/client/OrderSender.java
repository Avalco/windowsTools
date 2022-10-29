package avalco.network.vpn.client;


import avalco.network.vpn.base.orders.Order;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

public class OrderSender implements Runnable {
    private final BufferedWriter bufferedWriter;
    public OrderSender(BufferedWriter bufferedWriter){
        this.bufferedWriter=bufferedWriter;
    }
    private final LinkedBlockingQueue<Order> linkedBlockingQueue=new LinkedBlockingQueue<>();
    @Override
    public void run() {
        while (!Thread.interrupted()){
            try {
                Order order=linkedBlockingQueue.take();
               //logUtils.d("take:"+order);
                bufferedWriter.write(order.toString());
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void send(Order order){
        linkedBlockingQueue.offer(order);
    }

}
