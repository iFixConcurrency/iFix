package accountsubtype;

public class BusinessAccount extends Account {
  public BusinessAccount(int number, int amnt ) {
    super(number, amnt);
  }

  public void transfer(Account dest, int transferAmount){
	synchronized (this) {
		amount -= transferAmount;
		synchronized (dest) {
			dest.amount += transferAmount;
		}
	}
}
}
