class Test {

    private int one = 1;

    private int <<<<<<< changedTwo ||||||| two ======= changedAgain>>>>>>> = 2;

    private int three = 3;

    public int getChangedTwogetChangedAgain() {
        return <<<<<<< changedTwo ||||||| one ======= changedAgain>>>>>>>;
    }

    public int getOne() {
        return one;
    }

    public int getThree() {
        return three;
    }
}
