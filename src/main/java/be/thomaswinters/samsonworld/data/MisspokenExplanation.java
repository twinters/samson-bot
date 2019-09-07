package be.thomaswinters.samsonworld.data;

public class MisspokenExplanation {
    private final String versprekingText;
    private final String usedMisspokenWord;
    private final String actualWord;

    public MisspokenExplanation(String vraag, String usedMisspokenWord, String actualWord) {
        this.versprekingText = vraag;
        this.usedMisspokenWord = usedMisspokenWord;
        this.actualWord = actualWord;
    }

    public String getText() {
        return versprekingText;
    }

    public String getUsedMisspokenWord() {
        return usedMisspokenWord;
    }

    public String getActualWord() {
        return actualWord;
    }

    @Override
    public String toString() {
        return "TEXT:\n" + versprekingText +
                "\n(" + usedMisspokenWord + "->" + actualWord+")";
    }
}
