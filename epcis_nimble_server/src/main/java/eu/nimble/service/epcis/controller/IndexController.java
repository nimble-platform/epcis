package eu.nimble.service.epcis.controller;

import javax.validation.Valid;
import eu.nimble.service.epcis.form.Login;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import javax.validation.constraints.Null;
import java.util.List;

@Controller
@Scope("session")
public class IndexController {

    @Value("#{'${credential.username}'.split(',')}")
    private List<String> usernames;

    @Value("#{'${credential.password}'.split(',')}")
    private List<String> passwords;

    @Autowired
    HttpSession session;

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("login", new Login());
        return "index"; //view
    }

    @PostMapping("/")
    public String login(@Valid @ModelAttribute("login") Login login, BindingResult bindingResult,
                        @RequestParam(required = true) String userName,
                        @RequestParam(required = true) String passWord, HttpSession session) {

        if(bindingResult.hasErrors()) {
            return "redirect:/";
        }

        for(String username : usernames){
            if(username.equals(userName)){
                Integer passwordIndex=usernames.indexOf(username);
                if(passWord.equals(passwords.get(passwordIndex))) {
                    session.setAttribute("accessToken", "biba");
                    return "redirect:/home";
                }
            }
        }
        return "redirect:/";
    }

    @GetMapping("/home")
    public String index(){
        if(checkAuthentication()) {
            return "home";
        }
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("accessToken");
        return "redirect:/";
    }

    @GetMapping("/json-event-capture")
    public String jsonEventCapture() {
        if(checkAuthentication()) {
            return "json-event-capture";
        }
        return "redirect:/";
    }

    @GetMapping("/json-event-query")
    public String jsonEventQuery() {
        if(checkAuthentication()) {
            return "json-event-query";
        }
        return "redirect:/";
    }

    @GetMapping("/json-master-single-capture")
    public String jsonMasterSingleCapture() {
        if(checkAuthentication()) {
            return "json-master-single-capture";
        }
        return "redirect:/";
    }

    @GetMapping("/json-master-multiple-capture")
    public String jsonMasterMultipleCapture() {
        if(checkAuthentication()) {
            return "json-master-multiple-capture";
        }
        return "redirect:/";
    }

    @GetMapping("json-master-query")
    public String jsonMasterQuery() {
        if(checkAuthentication()) {
            return "json-master-query";
        }
        return "redirect:/";
    }

    @GetMapping("/xml-event-capture")
    public String xmlEventCapture() {
        if(checkAuthentication()) {
            return "xml-event-capture";
        }
        return "redirect:/";
    }

    @GetMapping("/xml-event-query")
    public String xmlEventQuery() {
        if(checkAuthentication()) {
            return "xml-event-query";
        }
        return "redirect:/";
    }

    @GetMapping("/xml-master-capture")
    public String xmlMasterCapture() {
        if(checkAuthentication()) {
            return "xml-master-capture";
        }
        return "redirect:/";
    }

    @GetMapping("xml-master-query")
    public String xmlMasterQuery() {
        if(checkAuthentication()) {
            return "xml-master-query";
        }
        return "redirect:/";
    }

    @GetMapping("/json-production-capture")
    public String jsonProductionCapture() {
        if(checkAuthentication()) {
            return "json-production-capture";
        }
        return "redirect:/";
    }

    @GetMapping("/json-production-query")
    public String jsonProductionQuery() {
        if(checkAuthentication()) {
            return "json-production-query";
        }
        return "redirect:/";
    }

    private boolean checkAuthentication() {
        if( session.getAttribute("accessToken") == null || !session.getAttribute("accessToken").equals("biba")) {
            return false;
        }
        return true;
    }
}
