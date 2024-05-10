public class Main {
  public static void main(String[] args) {
    int weekday = 4;
    int next = 9;
    switch (weekday) {
      case 1:
        System.out.println("Moneyday");
        if(1==1){
            weekday = 5;
        }
        break;
      case 2:
        System.out.println("Chewsday");
        break;
      case 3:
        System.out.println("Wednesday");
        break;
      case 4:
        System.out.println("Thursday");
        break;
      case 5:
        System.out.println("Friday");
        break;
      case 6:
        System.out.println("Saturday");
        break;
      case 7:
        System.out.println("Sunday");
        break;
    }
  }
}

