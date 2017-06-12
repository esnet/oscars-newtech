package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.acct.ent.CustomerE;
import net.es.oscars.acct.svc.CustService;
import net.es.oscars.dto.acct.Customer;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;


@Slf4j
@Controller
public class AdminAcctController {

    @Autowired
    public AdminAcctController(CustService custService) {
        this.custService = custService;
    }

    private CustService custService;

    private ModelMapper modelMapper = new ModelMapper();

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.info("user requested a resource which didn't exist", ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.info("user data conflicts ", ex);
    }


    @RequestMapping(value = "/admin/cust_list", method = RequestMethod.GET)
    public String admin_comp_list(Model model) {

        List<Customer> customers = custService.findAll().stream()
                .map(this::convertToDto).collect(Collectors.toList());

        model.addAttribute("customers", customers);
        return "admin_cust_list";
    }

    @RequestMapping(value = "/admin/cust_edit/{name}", method = RequestMethod.GET)
    public String admin_cust_edit(@PathVariable String name, Model model) {
        Customer customer = this.convertToDto(custService.findByName(name).orElseThrow(NoSuchElementException::new));
        log.info(customer.toString());

        model.addAttribute("customer", customer);
        return "admin_cust_edit";
    }


    @RequestMapping(value = "/admin/cust_update_submit", method = RequestMethod.POST)
    public String admin_user_update_submit(@ModelAttribute Customer updatedCustomer) {
        String name = updatedCustomer.getName();
        CustomerE customerE = custService.findByName(name).orElseThrow(NoSuchElementException::new);

        Long id = customerE.getId();
        customerE = convertToEnt(updatedCustomer);
        customerE.setId(id);
        custService.save(customerE);

        return "redirect:/admin/cust_edit/" + name;
    }

    private CustomerE convertToEnt(Customer dtoCustomer) {
        return modelMapper.map(dtoCustomer, CustomerE.class);
    }

    private Customer convertToDto(CustomerE customerE) {
        return modelMapper.map(customerE, Customer.class);
    }

}