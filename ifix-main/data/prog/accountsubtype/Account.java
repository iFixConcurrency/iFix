package accountsubtype;

public abstract class Account {
  protected String  name;
  protected int number;
  protected int amount;

  public Account(int number, int initialBalance) {
	this.name = "Account "+number;
        this.number = number;
        this.amount = initialBalance;
  }

  public String getName(){
	synchronized (this) {
		return name;
	}
}

  public int getBalance(){
	synchronized (this) {
		return amount;
	}
}

  public void deposit(int money){
	synchronized (this) {
		amount += money;
	}
}

  public void withdraw(int money){
	synchronized (this) {
		amount -= money;
	}
}

  // Implementing methods should be synchronized
  public abstract void transfer(Account dest, int mn);

  void print() {
	synchronized (this) {
	}
}

}
