package com.blog_api_core.controllers;

import com.blog_api_core.exceptions.NotFoundException;
import com.blog_api_core.models.*;
import com.blog_api_core.payload.BookMarkPayload;
import com.blog_api_core.payload.LikesPayload;
import com.blog_api_core.payload.PostSummary;
import com.blog_api_core.payload.SinglePost;
import com.blog_api_core.repository.BookMarkRepository;
import com.blog_api_core.repository.LikeRepository;
import com.blog_api_core.repository.UserRepository;
import com.blog_api_core.services.BookMarkService;
import com.blog_api_core.services.LikeService;
import com.blog_api_core.services.PostService;
import com.blog_api_core.services.TopicService;
import com.blog_api_core.utils.S3FileStorageUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@CrossOrigin
@RequestMapping("/blog")
public class PostController {
    private final PostService postService;
    private final TopicService topicService;
    private final S3FileStorageUtils s3FileStorageUtils;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final BookMarkRepository bookMarkRepository;
    private final LikeService likeService;
    private final BookMarkService bookMarkService;

    public PostController(PostService postService, TopicService topicService, S3FileStorageUtils s3FileStorageUtils, UserRepository userRepository, LikeRepository likeRepository, BookMarkRepository bookMarkRepository, LikeService likeService, BookMarkService bookMarkService) {
        this.postService = postService;
        this.topicService = topicService;
        this.s3FileStorageUtils = s3FileStorageUtils;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.bookMarkRepository = bookMarkRepository;
        this.likeService = likeService;
        this.bookMarkService = bookMarkService;
    }


    @PostMapping(value="/add-post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> addPost(
            @RequestPart("post") @Valid Post post,
            @RequestPart("image") MultipartFile file) {
//        get the currently logged-in user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByUsername(username);
        List<Topic> topics = post.getTopics();
//        check if topics already exist or create new ones
        List<Topic> existingTopics = new ArrayList<>();
        // Link each topic to the post
        if(topics != null && !topics.isEmpty()) {
            for (Topic topic : topics) {
//              Checking if topic exists by name
                Optional<Topic> existingTopic = topicService.getTopicByName(topic.getName());

                if(existingTopic.isPresent()) {
                    existingTopics.add(existingTopic.get());
                }else{
                    Topic newTopic = new Topic();
                    newTopic.setName(topic.getName());
                    existingTopics.add(topicService.saveTopic(newTopic));
                }
//                topic.setPost(post); // Set the post_id in each topic (ManyToOne relation)
            }
            post.setTopics(existingTopics);
        }

//        upload the post image too
        if(file != null && !file.isEmpty()) {
            String filePath = s3FileStorageUtils.uploadPostImage(file);
            post.setImgUrl(filePath);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        try{
            Post savedPost = postService.savePost(user.get(), post);

            response.put("status", "true");
            response.put("result", savedPost);
            response.put("username", username);
        } catch (NotFoundException e) {
            throw new NotFoundException("Post not saved");
        }

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/posts")
    public ResponseEntity<Map<String, Object>> getAllPosts(){
        Map<String, Object> response = new LinkedHashMap<>();
        List<Post> posts = postService.getAllPosts();

        response.put("status", true);
        response.put("result", posts);
        return ResponseEntity.ok(response);
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping("/posts-summary")
    public ResponseEntity<Map<String, Object>> getAllPostsSummary(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Please Log in"));
        Map<String, Object> response = new LinkedHashMap<>();
        List<PostSummary> posts = postService.getAllPostSummaries(user.getId());

        response.put("status", true);
        response.put("result", posts);
        response.put("user", SecurityContextHolder.getContext().getAuthentication().getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/summaries")
    public ResponseEntity<Map<String, Object>> getPaginatedPostSummaries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<PostSummary> summaries = postService.getPaginatedPostSummaries(page, size);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", true);
        response.put("result", summaries.getContent());
        response.put("currentPage", summaries.getNumber());
        response.put("totalItems", summaries.getTotalElements());
        response.put("totalPages", summaries.getTotalPages());
        response.put("hasNext", summaries.hasNext());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/{post_id}")
    public ResponseEntity<Map<String, Object>> getPostById(@PathVariable Long post_id){
        Map<String, Object> response = new LinkedHashMap<>();
        SinglePost post = postService.getSinglePostById(post_id);

        if(post == null) {
            throw new NotFoundException("Post not found");
        }

        response.put("status", true);
        response.put("result", post);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{post_id}")
    public ResponseEntity<Map<String, Object>> deletePost(@PathVariable Long post_id){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Please Log in"));

        Map<String, Object> response = new LinkedHashMap<>();
        Post post = postService.getPostById(post_id);
        if(post == null) {
            throw new NotFoundException("Post not found");
        } else if(!post.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("You are not authorized to delete this post");
        } else{
            postService.deletePost(post);
            response.put("status", true);
            response.put("message", "Post deleted successfully");
            return ResponseEntity.ok(response);
        }
    }



    @PostMapping("/like/{post_id}")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long post_id){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Please Log in"));
        Post post = postService.getPostById(post_id);

        Map<String, Object> response = new LinkedHashMap<>();

        Optional<Like> alreadyLiked = likeRepository.findByUserAndPost(user, post);
        if(alreadyLiked.isPresent()) {
            likeRepository.delete(alreadyLiked.get());
            response.put("status",true);
            response.put("message","unliked");
            return ResponseEntity.ok(response);
        }

        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
        likeRepository.save(like);
        response.put("status",true);
        response.put("message","liked");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bookmark/{post_id}")
    public ResponseEntity<Map<String, Object>> toggleBookMark(@PathVariable Long post_id){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Please Log in"));
        Post post = postService.getPostById(post_id);

        Map<String, Object> response = new LinkedHashMap<>();

        Optional<BookMark> alreadyBookedMarked = bookMarkRepository.findByUserAndPost(user, post);
        if(alreadyBookedMarked.isPresent()) {
            bookMarkRepository.delete(alreadyBookedMarked.get());
            response.put("status",false);
            response.put("message","Removed from bookmarks");
            return ResponseEntity.ok(response);
        }

        BookMark bookMark = new BookMark();
        bookMark.setUser(user);
        bookMark.setPost(post);
        bookMarkRepository.save(bookMark);
        response.put("status",true);
        response.put("message","Bookmarked");
        return ResponseEntity.ok(response);
    }


//    get articles by topic
    @GetMapping("/posts/topic/{topic_id}")
    public ResponseEntity<Map<String, Object>> getPostsByTopic(@PathVariable Long topic_id){
        Map<String, Object> response = new LinkedHashMap<>();
        List<PostSummary> posts = postService.getPostSummariesByTopicId(topic_id);

        if(posts == null) {
            throw new NotFoundException("Post not found");
        }

        response.put("status", true);
        response.put("result", posts);
        return ResponseEntity.ok(response);
    }
// Get posts of currently logged in user
    @GetMapping("/posts/user")
    public ResponseEntity<Map<String, Object>> getPostsByUser(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("Please Log in"));
        List<PostSummary> posts = postService.getPostSummariesByUserId(user.getId());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", true);
        response.put("result", posts);
        return ResponseEntity.ok(response);
    }

//    Get posts by userId
@GetMapping("/posts/user/{username}")
public ResponseEntity<Map<String, Object>> getPostsByUser(@PathVariable String username){
    List<PostSummary> posts = postService.getPostsByUsername(username);
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("status", true);
    response.put("result", posts);
    return ResponseEntity.ok(response);
}
@GetMapping("/post/likes/{post_id}")
public ResponseEntity<Map<String, Object>> getPostLikes(@PathVariable Long post_id){
    Map<String, Object> response = new LinkedHashMap<>();
    Post post = postService.getPostById(post_id);
    if(post == null) {
        throw new NotFoundException("Post not found");
    }
    List<LikesPayload> likes = likeService.getAllLikesOfPost(post_id);
    response.put("status", true);
    response.put("result", likes);
    return ResponseEntity.ok(response);
    }

    @GetMapping("/post/bookmarks/{post_id}")
    public ResponseEntity<Map<String, Object>> getPostBookMarks(@PathVariable Long post_id){
        Map<String, Object> response = new LinkedHashMap<>();
        Post post = postService.getPostById(post_id);
        if(post == null) {
            throw new NotFoundException("Post not found");
        }
        List<BookMarkPayload> bookMarks = bookMarkService.getAllBookMarksOfPost(post_id);
        response.put("status", true);
        response.put("result", bookMarks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookmarks/{user_id}")
    public ResponseEntity<Map<String, Object>> getPostByBookMarks(@PathVariable Long user_id){
        Map<String, Object> response = new LinkedHashMap<>();
        List<PostSummary> bookMarks = postService.getPostByBookMarksId(user_id);
        response.put("status", true);
        response.put("result", bookMarks);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchPost(@RequestParam String searchTerm){
        Map<String, Object> response = new LinkedHashMap<>();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Please Log in"));
        List<PostSummary> posts = postService.getAllPostSummaries(user.getId());
        List<PostSummary> filteredPosts = new ArrayList<>();

        for(PostSummary post : posts) {
            if(post.getTitle().toLowerCase().contains(searchTerm.toLowerCase())) {
                filteredPosts.add(post);
            }
        }

        response.put("status", true);
        response.put("result", filteredPosts);
        return ResponseEntity.ok(response);
    }
}
