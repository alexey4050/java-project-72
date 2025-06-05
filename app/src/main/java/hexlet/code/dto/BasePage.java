package hexlet.code.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter


public class BasePage {
    private String flashType;
    private String flashMessage;

    public void setFlash(String type, String message) {
        this.flashType = type;
        this.flashMessage = message;
    }
}
