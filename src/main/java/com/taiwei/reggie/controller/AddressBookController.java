package com.taiwei.reggie.controller;

import com.taiwei.reggie.common.BaseContext;
import com.taiwei.reggie.common.R;
import com.taiwei.reggie.entity.AddressBook;
import com.taiwei.reggie.service.AddressBookService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Delivery addresses of the logged-in customer. Every operation is scoped to the caller's own addresses.
 */
@RestController
@RequestMapping("/addressBook")
@Tag(name = "Address book (customer)")
public class AddressBookController {

    private final AddressBookService addressBookService;

    public AddressBookController(AddressBookService addressBookService) {
        this.addressBookService = addressBookService;
    }

    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        return R.success(addressBookService.addFor(BaseContext.getCurrentID(), addressBook));
    }

    @PutMapping
    public R<AddressBook> update(@RequestBody AddressBook addressBook) {
        return R.success(addressBookService.updateFor(BaseContext.getCurrentID(), addressBook));
    }

    @PutMapping("default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        return R.success(addressBookService.setDefault(BaseContext.getCurrentID(), addressBook.getId()));
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        addressBookService.deleteOwned(BaseContext.getCurrentID(), ids);
        return R.success("deleted");
    }

    @GetMapping("/{id}")
    public R<AddressBook> get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.findOwned(BaseContext.getCurrentID(), id);
        return addressBook != null ? R.success(addressBook) : R.error("address not found");
    }

    @GetMapping("default")
    public R<AddressBook> getDefault() {
        AddressBook addressBook = addressBookService.findDefault(BaseContext.getCurrentID());
        return addressBook != null ? R.success(addressBook) : R.error("no default address");
    }

    /** The most recently updated address. */
    @GetMapping("/lastUpdate")
    public R<AddressBook> lastUpdate() {
        List<AddressBook> all = addressBookService.listFor(BaseContext.getCurrentID());
        return all.isEmpty() ? R.error("no address") : R.success(all.stream()
                .max((a, b) -> a.getUpdateTime().compareTo(b.getUpdateTime())).orElse(all.get(0)));
    }

    @GetMapping("/list")
    public R<List<AddressBook>> list() {
        return R.success(addressBookService.listFor(BaseContext.getCurrentID()));
    }
}
