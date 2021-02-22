import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class QueueManager {
    private int servingTicketId;
    private Queue<Ticket> tickets = new LinkedList<>();
    private long lastTicketNum = 0;
    Timer newTimer;
    Counter counter;
    Queue<Customer> queuedCustomers = new LinkedList<>();

    public QueueManager(Counter counter) {
        this.counter = counter;
    }

    public void generateTickets() {
        int randomQNum = this.getRandomQueueNumber();
        for (int i=0; i<randomQNum; i++) {
           Ticket ticket = generateTicket();
           this.tickets.add(ticket);
        }
    }

    public Ticket generateTicket() {
        int counterNum = this.counter.getId();
        long lastTicketNumber = getLastTicketNumber();
        Long ticketNum = Long.parseLong(String.valueOf(counterNum) + String.valueOf(lastTicketNumber));
        return new Ticket(111, counterNum, ticketNum); //ticket created with random time interval assigned
    }

    public int getRandomQueueNumber() {
        int min = 1;
        int max = 10;
        int randomQueueNumber = (int)(Math.random() * (max - min + 1) + min);
        System.out.println(randomQueueNumber);
        return randomQueueNumber;
    }

    public long getLastTicketNumber() {
        return ++lastTicketNum;
    }

    public Queue<Ticket> getTickets() {
        return this.tickets;
    }

    public void getNextTicket() {
        QueueManager queueManager = this;
        Ticket nextTicket = this.tickets.peek(); 
        long period = (long)(nextTicket.getTicketTimeInterval() * 1000);
        // call a completeablefuture here?
        //then in supplier function, will have timer
        newTimer = new Timer();
        newTimer.schedule(new TicketHelper(queueManager), period, period); //start ticket serving scheduler
    }

    class TicketHelper extends TimerTask {
        public QueueManager queueManager;
        public Queue<Ticket> tickets;
        Customer currentServingCustomer = null;

        public TicketHelper(QueueManager queueManager) {
            this.queueManager = queueManager;
            this.tickets = queueManager.getTickets();
        }

        public void run() { 
            Ticket nextTicket = this.tickets.peek(); 
            long period = (long)(nextTicket.getTicketTimeInterval() * 1000);
            long currentServingTicketNumber = nextTicket.getTicketNumber();
            
            Timer oldTimer;
           if (!nextTicket.isExpired()) {
                System.out.println("Currently serving: " + currentServingTicketNumber);
                //search queuedCustomers list and if current serving ticket number matches a queuedCustomer's ticket number,
                //set that queuedCustomer's isInQueue attr to false (setting of alert at the same time) and poll this queuedCustomer from the list
                for (Customer queuedCustomer: queuedCustomers) {
                    if (queuedCustomer.getTicket().getTicketNumber() == currentServingTicketNumber) {
                        currentServingCustomer = queuedCustomer;
                        System.out.println("SERVING CUSTOMER " + currentServingCustomer.getUsername());
                    }
                }

                // CompletableFuture.supplyAsync(supplier)

                CompletableFuture.delayedExecutor(period, TimeUnit.MILLISECONDS).execute(() -> {
                    // code here executes after {period} seconds
                    if (currentServingCustomer != null) { // clear customer from queue if got current serving customer
                        System.out.println("FINISHED SERVING CUSTOMER " + currentServingCustomer.getUsername() + " AFTER " + TimeUnit.MILLISECONDS.toSeconds(period) + " seconds.");
                        currentServingCustomer.setInQueue(false);
                        currentServingCustomer.setTicket(null);
                        queuedCustomers.poll(); //remove current customer from queue
                    }
                    nextTicket.setExpired(true); //expire ticket after serving
                });
                
           } else {
                oldTimer = newTimer; //assign previous timer as oldtimer so it can be cancelled and purge after new timer starts
                
                this.tickets.poll(); //remove served ticket

                if (this.tickets.peek() != null) { //check for remaining tickets
                    newTimer = new Timer(); //instantiate a new timer
                    // nextTicket = this.tickets.peek(); 
                    // long period = (long)(nextTicket.getTicketTimeInterval() * 1000);
                    newTimer.schedule(new TicketHelper(this.queueManager), 0, period);
                } else { //no more tickets left
                    System.out.println("~~ Finished serving all tickets for counter " + this.queueManager.counter.getId() + "~~");
                    this.queueManager.run(); //gen new batch of tickets
                }

                oldTimer.cancel();
                oldTimer.purge();
           }
        } 
    }

    public void run() {
        this.generateTickets();
        System.out.println("~~Generated new batch of tickets for counter " + this.counter.getId() + " ~~");
        for (Ticket ticket : tickets) {
            System.out.println("Ticket number: " + ticket.getTicketNumber() + " with time interval: " + ticket.getRandomTimeInterval());
        }
        this.getNextTicket();
    }

    public void handleTicketRequest(Customer customer) {
        //generate a single ticket for this customer and add to tickets list
        // add to queuedCustomers list
        // set this customer's isInQueue attr to true
        Ticket newTicket = this.generateTicket();
        System.out.println("QUEUED CUSTOMER " + customer.getUsername() + " WITH TICKET NUMBER " + newTicket.getTicketNumber());
        customer.setTicket(newTicket);
        customer.setInQueue(true);
        this.tickets.add(newTicket);
        queuedCustomers.add(customer);
    }

    public int getServingTicketId() {
        return servingTicketId;
    }

    public void setServingTicketId(int servingTicketId) {
        this.servingTicketId = servingTicketId;
    }
    
}
