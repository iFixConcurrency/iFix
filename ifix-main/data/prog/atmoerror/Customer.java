package atmoerror;

public class Customer implements Runnable {

    private BankAccount account;
    private int cash;

    public Customer(int cash, BankAccount account) {
        this.cash = cash;
        this.account = account;
    }

    public void cost(int n) {
        cash -= n;
        account.add(n);
    }

    @Override
    public void run() {

        while (cash > 0) {
            cost(1);
        }

        System.out.println("total: " + account.getTotal());
        /*if (account.getTotal() != 10)
            throw new RuntimeException();*/
    }

}
