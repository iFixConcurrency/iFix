package accountsubtype;

public class PersonalAccount extends Account {
  public PersonalAccount(int number, int initialBalance) {
    super(number, initialBalance);
  }

  public void transfer(Account ac, int mn){
	synchronized (this) {
		amount -= mn;
		ac.amount += mn;
	}
}
}
