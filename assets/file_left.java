/**
 * This is a simple Java program.
 */
public class Main {
  /**
   * This is the main method which makes use of the switch case.
   * @param args Unused.
   */
  // THis is a test
  public static void main(String[] args) {
    // Switching on dayOfWeek.
    int dayOfWeek = 4;

    int next = 9;

    switch (dayOfWeek) {
      case 1:
        System.out.println("Monday");
        if(1==1){
          dayOfWeek = 5;
        }
        break;
      case 2:
        System.out.println("Tuesday");
        break;
      case 3:
        System.out.println("Wednesday");
        break;
      case 5:
        System.out.println("Friday");
        break;
      case 4:
        System.out.println("Thursday");
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

