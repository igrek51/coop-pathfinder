package igrek.robopath;

import org.springframework.stereotype.Component;

@Component
public class DefaultAwesomeActionService implements AwesomeActionService {

    @Override
    public String processName(final String name) {
        if(name.equals("dupa")) {
            return "Hello Dupa!";
        }
        else {
            return "Hello Unknown Stranger!";
        }
    }

}
