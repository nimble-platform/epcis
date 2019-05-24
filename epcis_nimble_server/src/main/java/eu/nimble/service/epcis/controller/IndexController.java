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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@Scope("session")
public class IndexController {

    @Value("#{'${credential.username}'.split(',')}")
    private List<String> usernames;

    @Value("#{'${credential.password}'.split(',')}")
    private List<String> passwords;

    @Value("#{'${credential.accessToken}'.split(',')}")
    private List<String> accessTokens;

    @Autowired
    HttpSession session;

    @GetMapping("/")
    public String main(Model model) {
        model.addAttribute("login", new Login());
        session.removeAttribute("invalidCredential");
        return "index"; //view
    }

    @PostMapping("/")
    public ModelAndView login(@Valid Login login, BindingResult bindingResult, HttpSession session) {
        ModelAndView modelAndView = new ModelAndView();
        if(bindingResult.hasErrors()) {
            session.removeAttribute("invalidCredential");
            modelAndView.addObject("login", login);
            modelAndView.setViewName("index");
            return modelAndView;
        }

        boolean authenticateUser = false;

        for(String username : usernames){
            if(username.equals(login.getUserName())){
                Integer userIndex=usernames.indexOf(username);
                if(login.getPassWord().equals(passwords.get(userIndex))) {
                    session.setAttribute("accessToken", accessTokens.get(userIndex));
                    modelAndView.setViewName("home");
                    authenticateUser = true;
                    break;
                }
            }
        }

        if(!authenticateUser) {
            session.setAttribute("invalidCredential", "Username or Password is not correct!");
            modelAndView.setViewName("index");
        }

        return modelAndView;
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
        if( session.getAttribute("accessToken") == null) {
            return false;
        }
        return true;
    }
}
