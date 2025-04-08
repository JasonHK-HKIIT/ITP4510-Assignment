import java.util.InputMismatchException;
import java.util.Scanner;

public class CashierSim
{
    private static Scanner scanner;

    public static void main(String[] args)
    {
        scanner = new Scanner(System.in);

        System.out.println("--------------- SETUP SIMULATION ENVIRONMENT ---------------");

        // Input and validate the simulation length.
        var minutes = 0;
        do
        {
            System.out.print("Input simulation length (min): ");
            try
            {
                minutes = scanner.nextInt();
                if (minutes < 1) { System.err.println("The simulation length must be ≥ 1."); }
            }
            catch (InputMismatchException ex)
            {
                System.err.println("Please enter an integer.");
            }
            finally
            {
                scanner.nextLine();
            }
        }
        while (minutes < 1);

        // Input and validate the number of counters.
        var maxTellers = 0;
        do
        {
            System.out.print("Input number of counters: ");
            try
            {
                maxTellers = scanner.nextInt();
                if (maxTellers < 1) { System.err.println("The number of counters must be ≥ 1."); }
            }
            catch (InputMismatchException ex)
            {
                System.err.println("Please enter an integer.");
            }
            finally
            {
                scanner.nextLine();
            }
        }
        while (maxTellers < 1);

        System.out.println();
        runSimulation(minutes, maxTellers);
    }

    private static void runSimulation(int minutes, int maxTellers)
    {
        // Initialize the tellers.
        var tellers = new Teller[maxTellers];
        for (var i = 0; i < maxTellers; i++) { tellers[i] = new Teller(); }

        System.out.println("--------------- START SIMULATION ---------------");
        System.out.println();

        // Variables to store the statistics.
        var servedCount = 0;
        var cumulatedQueueLength = 0;
        var maxQueueLength = 0;
        var cumulatedWaitTime = 0;
        var maxWaitTime = 0;

        // The waiting line.
        var waitLine = new ListQueue();

        // Do the iterations.
        for (var minute = 1; minute <= minutes; minute++)
        {
            System.out.printf("At the beginning of iteration %d...%n", minute);

            // Handle the customers, if any.
            for (var teller : tellers) { teller.handle(minute); }

            // Input and validate the serving time.
            var servingTime = -1;
            do
            {
                System.out.print("Input serving time for a new customer: ");
                try
                {
                    servingTime = scanner.nextInt();
                    if (servingTime < 0) { System.err.println("The serving time must be ≥ 0."); }
                }
                catch (InputMismatchException ex)
                {
                    System.err.println("Please enter an integer.");
                }
                finally
                {
                    scanner.nextLine();
                }
            }
            while (servingTime < 0);

            // Let the new customer queued in the waiting line.
            if (servingTime > 0) { waitLine.enqueue(new Customer(servingTime, minute)); }

            if (!waitLine.isEmpty())
            {
                // Loop though the tellers to find one that's available.
                for (var teller : tellers)
                {
                    if (teller.hasCustomer()) { continue; }
                    
                    // Serve the first customer in the waiting line.
                    var customer = (Customer) waitLine.dequeue();
                    teller.serve(customer, minute);
                    servedCount++;

                    // Calculate the statistics.
                    var waitTime = customer.getWaitTime(minute);
                    cumulatedWaitTime += waitTime;
                    maxWaitTime = Math.max(maxWaitTime, waitTime);

                    // Break early when the waiting line was empty.
                    if (waitLine.isEmpty()) { break; }
                }
            }

            // Calculate the statistics.
            maxQueueLength = Math.max(maxQueueLength, waitLine.count());
            cumulatedQueueLength += waitLine.count();

            System.out.printf("After %d minute(s) ##%n", minute);
            for (var i = 0; i < maxTellers; i++)
            {
                System.out.printf("    Teller %d: %s", i + 1, tellers[i]);
            }
            System.out.printf("    Waiting Line: %s%n", waitLine);
            System.out.println();
        }

        System.out.println("--------------- END OF SIMULATION ---------------");
        System.out.printf("Total minutes simulated   : %d minute(s)%n", minutes);
        System.out.printf("Number of tellers         : %d teller(s)%n", maxTellers);
        System.out.printf("Number of customers served: %d customer(s)%n", servedCount);
        System.out.printf("Average queue length      : %.2f customer(s)%n", (double) cumulatedQueueLength / minutes);
        System.out.printf("Maximum queue length      : %d customer(s)%n", maxQueueLength);
        System.out.printf("Average wait time         : %.2f minute(s)%n", (servedCount == 0) ? 0 : ((double) cumulatedWaitTime / servedCount));
        System.out.printf("Maximum wait time         : %d minute(s)%n", maxWaitTime);
    }
}

/**
 * A teller.
 */
class Teller
{
    private int availableAt = 0;
    private Customer customer = null;

    /**
     * Initialize a new teller.
     */
    public Teller() {}

    /** The minute this teller will be available serving customer. */
    public int getAvailableAt() { return availableAt; }

    /** Is the teller currently serving a customer. */
    public boolean hasCustomer() { return (customer != null); }

    /**
     * Handle the customer, if any. Let the customer leave when it's done.
     * 
     * @param minute The minute currently at.
     */
    public void handle(int minute)
    {
        if (hasCustomer() && (availableAt <= minute)) { customer = null; }
    }

    /**
     * Serve a new customer.
     * 
     * @param customer The customer to be served.
     * @param minute   The minute currently at.
     */
    public void serve(Customer customer, int minute)
    {
        if (hasCustomer()) { throw new TellerBusyException(); }

        this.customer = customer;
        this.availableAt = minute + customer.getServingTime();
    }

    @Override
    public String toString()
    {
        return Integer.toString(availableAt);
    }
}

/**
 * A customer.
 */
class Customer
{
    private final int servingTime;
    private final int arrivedAt;

    /**
     * Initialize a new customer.
     * 
     * @param servingTime The time (in minutes) needed to serve this customer.
     * @param arrivedAt   The minute this customer arrived at.
     */
    public Customer(int servingTime, int arrivedAt)
    {
        this.servingTime = servingTime;
        this.arrivedAt = arrivedAt;
    }

    /** The time (in minutes) needed to serve this customer. */
    public int getServingTime() { return servingTime; }

    /**
     * Calculate wait time of this customer.
     * 
     * @param minute The minute currently at.
     */
    public int getWaitTime(int minute)
    {
        return (minute - arrivedAt);
    }

    @Override
    public String toString()
    {
        return Integer.toString(servingTime);
    }
}

class TellerBusyException extends IllegalStateException
{
    public TellerBusyException()
    {
        super("The teller is currently busy serving a customer.");
    }
}
