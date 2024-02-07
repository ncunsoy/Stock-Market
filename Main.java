public class Main {
    public static void main(String[] args) {
        Trader trader = new Trader();

        // Sample orders
        Order order1 = new Order("AAPL", "buy", 150.0, 100, 1, "user1", false);
        Order order2 = new Order("GOOGL", "sell", 250.0, 50, 2, "user2", true);
        Order order3 = new Order("MSFT", "buy", 180.0, 200, 3, "user3", false);
        Order order4 = new Order("AAPL", "sell", 150.0, 100, 4, "user4");
        Order order5 = new Order("AAPL", "sell", 120.00, 50, 5, "user5");
        Order order6 = new Order("AAPL", "sell", 75.00, 80, 6, "user6",true);
        Order order7=new Order("GOOGL", "buy", 270.00, 50, 7, "user7", true);
        Order order8=new Order("GOOGL", "sell", 230.00, 50, 8, "user8", false);
        Order cancelOrder = new Order("AAPL", "Cancel", 0.0, 0, 5, "user5", false);
        Order cancelOrder2 = new Order("GOOGL", "Cancel", 0.0, 0, 2, "user1", false);
        Order cancelOrder3 = new Order("GOOGL", "Cancel", 0.0, 0, 2, "user2", false);
        Order cancelOrder4 = new Order("GOOGL", "Cancel", 0.0, 0, 9, "user2", false);

        trader.pushOrder(order1);
        trader.pushOrder(order2);
        trader.pushOrder(order3);
        trader.pushOrder(order4);
        trader.pushOrder(order5);
        trader.pushOrder(order6);
        trader.pushOrder(order7);
        trader.pushOrder(order8);



        //Test 1
        //Alıcı için var olan seçeneklerden alıcı ucuz olan(lar)ı seçer.
        trader.execute();
        trader.printExecutedOrders();
        //user6	user1	75.0	80
        //user5	user1	120.0	20
        //user8 user7   230.00  50

        trader.printOrderQueue();
        //GOOGL sell 250.0  50   2   user2  true
        //MSFT  buy  180.0  200  3   user3  false
        //AAPL  sell  150.0  100  4  user4  false
        //AAPL  sell  120.00  30  5   user5  false



        //Test 2
        //Geçerli ve geçersiz cancel işlemleri
        trader.pushOrder(cancelOrder);  //Execute edilmiş order cancellanmaz. (AAPL  sell  120.00  30  5   user5  false) kısmen execute edildi.
        trader.pushOrder(cancelOrder2);    //User sadece kendi orderını cancellayabilir.
        trader.pushOrder(cancelOrder4);     //Cancel için timestamp aynı olmalıdır.
        trader.pushOrder(cancelOrder3);   //Geçerli cancel işlemi

        trader.printOrderQueue();
        //MSFT  buy    180.0   200    3   user3  false
        //AAPL  sell    150.0   100    4   user4  false
        //AAPL  sell    120.0   30    5   user5  false



        //Test 3
        //Aynı hisse senedini timestampi küçük olan alıcı alır.
        Order order9=new Order("MSFT", "buy", 180.0, 200, 9, "user9");
        Order order10 = new Order("MSFT", "sell", 120.00, 200, 10, "user10",true);
        trader.pushOrder(order9);
        trader.pushOrder(order10);
        trader.execute();

        trader.printExecutedOrders();
        //user10	user3	120.0	200

        trader.printOrderQueue();
        //AAPL  sell    150.0   100    4   user4  false
        //AAPL  sell    120.0   30    5   user5  false
        //MSFT  buy    180.0   200    9   user9  false


        //Test 4
        //allOrNone ı true olarak ayarlanmış bir order tek transaction yapar.(Daha cazip teklifler olsa bile)
        Order order11 = new Order("VAKBN", "buy", 150.0, 100, 11, "user11",true);
        Order order12 = new Order("VAKBN", "sell", 120.00, 50, 12, "user12");
        Order order13 = new Order("VAKBN", "sell", 130.00, 50, 13, "user13");
        Order order14 = new Order("VAKBN", "sell", 150.00, 100, 14, "user14",false);
        trader.pushOrder(order11);
        trader.pushOrder(order12);
        trader.pushOrder(order13);
        trader.pushOrder(order14);

        trader.execute();
        trader.printExecutedOrders();
        //user14	user11	150.0	100

        trader.printOrderQueue();
        // AAPL  sell    150.0   100    4   user4  false
        //AAPL  sell    120.0   30    5   user5  false
        //MSFT  buy    180.0   200    9   user9  false
        //VAKBN  sell    120.0   50    12   user12  false
        //VAKBN  sell    130.0   50    13   user13  false



        //Test 5
        //Bir alıcı için satıcıların teklifleri aynıysa timestamp e göre öncelik verilir.
        Order order15 = new Order("VAKBN", "buy", 120.00, 50, 15, "user15");
        Order order16 = new Order("VAKBN", "sell", 120.00, 50, 16, "user16",false);
        trader.pushOrder(order15);
        trader.pushOrder(order16);
        trader.execute();

        trader.printExecutedOrders();
        //user12    user15  120.00  50

        trader.printOrderQueue();
        //AAPL  sell    150.0   100    4   user4  false
        //AAPL  sell    120.0   30    5   user5  false
        //MSFT  buy    180.0   200    9   user9  false
        //VAKBN  sell    130.0   50    13   user13  false
        //VAKBN  sell    120.0   50    16   user16  false


        //Test 6
        //printExecutedOrders() metodu son printExecutedOrders() metodundan itibaren yapılan işleri basar.
        trader.execute();   //Hiçbir işlem yapılmadı.
        trader.printExecutedOrders();   //Hiçbir şey yazmaz.


        //Genel olarak executionlar
        Order order17=new Order("AAPL","buy",180.00,130,17,"user17");
        Order order18=new Order("MSFT","sell",165.00,200,18,"user18",true);
        Order order19=new Order("VAKBN","buy",145.00,50,19,"user19",true);
        Order order20=new Order("VAKBN","buy",130.00,50,20,"user20");

        trader.pushOrder(order17);
        trader.pushOrder(order18);
        trader.pushOrder(order19);
        trader.pushOrder(order20);

        trader.execute();
        trader.printExecutedOrders();
        //user18	user9	165.0	200
        //user5	    user17	120.0	30
        //user4	    user17	150.0	100
        //user16	user19	120.0	50
        //user13	user20	130.0	50

        trader.printOrderQueue(); //Hiçbir şey kalmadı.


        //Test 7
        //Alıcı ve satıcı aynı kişi olamaz.
        Order order21=new Order("GARAN","buy",85.00,125,21,"user21");
        Order order22=new Order("GARAN","sell",85.00,125,22,"user21");
        trader.pushOrder(order21);
        trader.pushOrder(order22);
        trader.execute();
        trader.printExecutedOrders();   //Hiçbir işlem gerçekleşmedi.
        trader.printOrderQueue();
        //GARAN  buy    85.0   125    21   user21  false
        //GARAN  sell    85.0   125    22   user21  false

    }
}
