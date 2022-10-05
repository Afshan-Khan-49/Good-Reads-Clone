package com.testproject.betterreads.user;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import com.testproject.betterreads.book.Book;
import com.testproject.betterreads.book.BookRepository;

@Controller
public class UserBooksController {

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserBooksRepository userBooksRepository;

    @Autowired
    BooksByUserRepository booksByUserRepository;

    @PostMapping("/addUserBook")
    public ModelAndView addUserBook(@RequestBody MultiValueMap<String, String> formData,
            @AuthenticationPrincipal OAuth2User principal) {
        // Only authenticated users are allowed to add books
        if (principal == null || principal.getAttribute("login") == null) {
            return null;
        }
        String bookId = formData.getFirst("bookId");
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (!optionalBook.isPresent()) {
            return new ModelAndView("redirect:/");
        }

        Book book = optionalBook.get();

        UserBooks userBooks = new UserBooks();
        UserBooksPrimaryKey userBooksPrimaryKey = new UserBooksPrimaryKey();
        userBooksPrimaryKey.setBookId(bookId);
        userBooksPrimaryKey.setUserId(principal.getAttribute("login"));
        userBooks.setKey(userBooksPrimaryKey);
        userBooks.setCompletedDate(LocalDate.parse(formData.getFirst("completedDate")));
        userBooks.setRating(Integer.parseInt(formData.getFirst("rating")));
        userBooks.setReadingStatus(formData.getFirst("readingStatus"));
        userBooks.setStartedDate(LocalDate.parse(formData.getFirst("startedDate")));
        userBooksRepository.save(userBooks);

        BooksByUser booksByUser = new BooksByUser();
        booksByUser.setAuthorNames(book.getAuthorNames());
        booksByUser.setBookId(bookId);
        booksByUser.setBookName(book.getName());
        booksByUser.setCoverIds(book.getCoverIds());
        booksByUser.setId(principal.getAttribute("login"));
        booksByUser.setRating(Integer.parseInt(formData.getFirst("rating")));
        booksByUser.setReadingStatus(formData.getFirst("readingStatus"));
        booksByUserRepository.save(booksByUser);

        return new ModelAndView("redirect:/books/" + bookId);
    }
}
