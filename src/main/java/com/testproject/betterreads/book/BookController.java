package com.testproject.betterreads.book;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.testproject.betterreads.user.UserBooks;
import com.testproject.betterreads.user.UserBooksPrimaryKey;
import com.testproject.betterreads.user.UserBooksRepository;

@Controller
public class BookController {

    private final String COVER_IMAGE_ROOT = "http://covers.openlibrary.org/b/id/";

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserBooksRepository userBooksRepository;
    // Once the books are fetched using the open library api this api will be called to get more details 
    // on the book
    @GetMapping(value = "/books/{bookId}")
    public String getBookById(@PathVariable String bookId, Model model, @AuthenticationPrincipal OAuth2User principal) {
        //System.out.println("Inside Books");

        Optional<Book> optionalBook = bookRepository.findById(bookId);
        String coverImageUrl = "/images/no-image.png";

        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();

            if (book.getCoverIds() != null && book.getCoverIds().size() > 0)
                coverImageUrl = COVER_IMAGE_ROOT + book.getCoverIds().get(0) + "-L.jpg";
            model.addAttribute("coverImage", coverImageUrl);
            model.addAttribute("book", book);
            // For a logged in user we will also show book tracking details
            if (principal != null && principal.getAttribute("login") != null) {
                String userId = principal.getAttribute("login");
                model.addAttribute("loginId", userId);
                UserBooksPrimaryKey userBooksPrimaryKey = new UserBooksPrimaryKey();
                userBooksPrimaryKey.setBookId(bookId);
                userBooksPrimaryKey.setUserId(userId);

                Optional<UserBooks> optionalUserBooks = userBooksRepository.findById(userBooksPrimaryKey);
                if (optionalUserBooks.isPresent()) {
                    model.addAttribute("userBooks", optionalUserBooks.get());
                } else {
                    model.addAttribute("userBooks", new UserBooks());
                }
            }
            return "book";
        }
        return "book-not-found";
    }

}
