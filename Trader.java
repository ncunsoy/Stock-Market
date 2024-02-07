import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Stack;

public class Trader{
     PriorityQueue<Order> trade=new PriorityQueue<>(Order::compareTo);
     ArrayList<String>executedOrders=new ArrayList<>();

    public void pushOrder(Order order) {
        if(order.type.equalsIgnoreCase("cancel"))
            cancel(order,trade);

        else
            trade.add(order);
    }

    public void execute() {
        PriorityQueue<Order> buyerQueue = new PriorityQueue<>(Order::compareTo);
        PriorityQueue<Order> sellerQueue = new PriorityQueue<>(Order::compareToForSeller); //Fiyata göre öncelik sıralaması yapması  için

        Stack<Order> buyerStack=new Stack<>();  //Her alıcı için tüm satıcıları kontrol etmek için kullanılan yardımcı veri yapısı.
        Stack<Order> sellerStack=new Stack<>();  //Her alıcı için tüm satıcıları kontrol etmek için kullanılan yardımcı veri yapısı.

        //Orderları buy ve sell olarak ikiye ayırıyorum.
        while (!trade.isEmpty()) {
            Order o = trade.remove();
            if (o.type.equals("buy"))
                buyerQueue.add(o);
            else
                sellerQueue.add(o);
        }

        //Her bir alıcı için tüm satıcıları gezip mümkün işlemleri yapıyorum.
        while(!buyerQueue.isEmpty()){
            Order buyer=buyerQueue.remove();

            while(!sellerQueue.isEmpty() && buyer.quantity>0){
                Order seller=sellerQueue.remove();

                if(buyer.stockSymbol.equals(seller.stockSymbol) && buyer.price>=seller.price && !buyer.userID.equals(seller.userID)){
                    //Alıcı ve satıcının allOrNone set etmesi durumu
                    if(buyer.allOrNone && seller.allOrNone){
                        if(buyer.quantity==seller.quantity){
                            executedOrders.add((""+seller.userID+"\t"+buyer.userID+"\t"+seller.price+"\t"+buyer.quantity));
                            buyer.quantity=0;
                            seller.quantity=0;
                        }

                    }

                    //Sadece alıcının allOrNone set etmesi durumu
                    else if(buyer.allOrNone) {
                        if(seller.quantity>= buyer.quantity){
                            executedOrders.add(("" + seller.userID + "\t" + buyer.userID + "\t" + seller.price + "\t" + buyer.quantity));
                            seller.quantity -= buyer.quantity;
                            buyer.quantity = 0;
                            seller.wasExecuted = true;
                        }
                    }

                    //Sadece satıcının allOrNone set etmesi durumu
                    else if(seller.allOrNone){
                        if(buyer.quantity>= seller.quantity){
                            executedOrders.add((""+seller.userID+"\t"+buyer.userID+"\t"+seller.price+"\t"+seller.quantity));
                            buyer.quantity-=seller.quantity;
                            seller.quantity=0;
                            buyer.wasExecuted=true;
                        }

                    }

                    //Ne alıcının ne de satıcının allOrNone set etmesi durumu
                    else{
                        if(buyer.quantity== seller.quantity){
                            executedOrders.add((""+seller.userID+"\t"+buyer.userID+"\t"+seller.price+"\t"+buyer.quantity));
                            buyer.quantity=seller.quantity=0;
                        }
                        else if(buyer.quantity> seller.quantity){
                            executedOrders.add((""+seller.userID+"\t"+buyer.userID+"\t"+seller.price+"\t"+seller.quantity));
                            buyer.quantity-=seller.quantity;
                            seller.quantity=0;
                            buyer.wasExecuted=true;
                        }
                        else{
                            executedOrders.add((""+seller.userID+"\t"+buyer.userID+"\t"+seller.price+"\t"+buyer.quantity));
                            seller.quantity-= buyer.quantity;
                            buyer.quantity=0;
                            seller.wasExecuted=true;
                        }
                    }
                }
                if(seller.quantity>0) sellerStack.push(seller); //Satıcının orderı tamamen satılmadıysa yardımcı veri yapıma ekliyorum.
            }
            //Her bir alıcı için baştan sona satıcıları gezip tamamen satılmayanları eklediğim veri yapımdaki orderları esas veri yapıma
            //tekrar ekleyerek her bir alıcı için kalan tüm satıcıları gezme işleminin mümkün olmasını sağlıyorum.
            while(!sellerStack.isEmpty()){
                sellerQueue.add(sellerStack.pop());
            }

            if(buyer.quantity>0) buyerStack.add(buyer); //Alıcı orderı tamamen almadıysa yardımcı veri yapıma ekliyorum.
        }

        //Kalan tüm orderları tekrar asıl listeye ekliyorum.
        while(!sellerQueue.isEmpty())
            trade.add(sellerQueue.remove());

        while(!buyerStack.isEmpty())
            trade.add(buyerStack.pop());

    }

    public void printExecutedOrders() {
        while(!executedOrders.isEmpty()){
            System.out.println(executedOrders.remove(0));
        }
        System.out.println();

    }

    public void printOrderQueue() {
        Stack<Order> stack=new Stack<>();

        while (!trade.isEmpty()){
            Order o=trade.remove();
            stack.push(o);
            System.out.println(o);
        }

        while (!stack.isEmpty())
            trade.add(stack.pop());
        System.out.println();
    }

    public void cancel(Order order, PriorityQueue<Order> queue){
        Stack<Order> stack=new Stack<>();
        boolean flag=false;

        while (!queue.isEmpty()){
            Order o=queue.remove();
            //Gelen order ile esas orderın aynı olup olmamasına ve daha önce execute edilip edilmediğine bakıyorum.
            if(o.nearlyEquals(order) && !o.wasExecuted){
                flag=true;
                break;
            }
            stack.push(o);
        }

        while (!stack.isEmpty())
            queue.add(stack.pop());

        //Eğer cancel işlemi gerçekleşmediyse gerçekleşmediğini yazıyorum.
        if(!flag)
            System.out.println("Invalid cancellation.\n");

    }
}


class Order implements Comparable{
    String stockSymbol;
   	String type;
    double price;
    int quantity;
    int timeStamp;
    String userID;
    boolean allOrNone;
    boolean wasExecuted;   //Orderın execute edilip edilmediğini tutmak için

    public Order(String stockSymbol,String type, double price,int quantity,int timeStamp,String userID,boolean allOrNone){
        this.stockSymbol=stockSymbol;
        this.type=type;
        this.price=price;
        this.quantity=quantity;
        this.timeStamp=timeStamp;
        this.userID=userID;
        this.allOrNone=allOrNone;
        wasExecuted=false;
    }

    public Order(String stockSymbol,String type, double price,int quantity,int timeStamp,String userID){
        this.stockSymbol=stockSymbol;
        this.type=type;
        this.price=price;
        this.quantity=quantity;
        this.timeStamp=timeStamp;
        this.userID=userID;
        this.allOrNone=false;
        wasExecuted=false;
    }

    //Cancel işlemi yaparken stockSymbol, timeStamp ve userID karşılaştırması yapması için
    public boolean nearlyEquals(Order order){
        return this.stockSymbol.equals(order.stockSymbol) && this.timeStamp == order.timeStamp && this.userID.equals(order.userID);
    }


    @Override
    //Orderları time stamplerine göre sıralama yapması için kullanılacak karşılaştırma metodu
    public int compareTo(Object o) {
        Order order=(Order) o;
        if(this.timeStamp<order.timeStamp)
            return -1;

        if(this.timeStamp>order.timeStamp)
            return 1;
        return 0;
    }

    //Satıcıları execute sırasında öncelikle fiyata, fiyatlar eşitse timeStamp e göre tutmak için kullanılacak karşılaştırma metodu
    public int compareToForSeller(Order order) {
        if(this.price<order.price)
            return -1;

        if(this.price>order.price)
            return 1;
        else{
            if(this.timeStamp<order.timeStamp)
                return -1;

            if(this.timeStamp>order.timeStamp)
                return 1;
            return 0;
        }
    }

    public String toString() {
        String str="";
            str+=stockSymbol+"  "+type+"    "+price+"   "+quantity+"    "+timeStamp+"   "+userID+"  "+allOrNone;
        return str;
    }
}





