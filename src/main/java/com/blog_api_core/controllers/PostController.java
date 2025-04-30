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

/**
 * Controller that handles all the API requests related to blog posts, including CRUD operations, likes, bookmarks, and file uploads.
 * This class is responsible for:
 * - Creating, reading, updating, and deleting posts
 * - Liking and unliking posts
 * - Bookmarking and unbookmarking posts
 * - Uploading files (e.g., images) to S3
 * - Retrieving posts based on various filters (e.g., topic, user)
 *
 * Dependencies injected:
 * - PostService: Handles business logic for managing posts
 * - TopicService: Manages topics related to posts
 * - S3FileStorageUtils: Handles file uploads to S3
 * - UserRepository: Manages user data and authentication
 * - LikeRepository: Manages the like functionality for posts
 * - BookMarkRepository: Manages the bookmark functionality for posts
 * - LikeService: Service layer for like-related operations
 * - BookMarkService: Service layer for bookmark-related operations
 */
@RestController
@CrossOrigin
@RequestMapping("/v1/blog")
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

/**
 * Handles creating a new post with image upload and topic management.
 * - Retrieves the currently logged-in user.
 * - Associates existing or newly created topics to the post.
 * - Uploads the post image to S3 storage.
 * - Saves the post and returns the saved post with user information.
 *
 * @param post the Post object (from multipart request part)
 * @param file the image file associated with the post
 * @return ResponseEntity containing the save status, saved post, and username
 */
@PostMapping(value="/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

/**
 * Retrieves all posts and returns them with a success status.
 *
 * @return ResponseEntity containing the status and list of posts
 */
@GetMapping("/posts")
public ResponseEntity<Map<String, Object>> getAllPosts(){
    Map<String, Object> response = new LinkedHashMap<>();
    List<Post> posts = postService.getAllPosts();

    response.put("status", true);
    response.put("result", posts);
    return ResponseEntity.ok(response);
}

/**
 * Retrieves all post summaries for the logged-in user.
 *
 * @return ResponseEntity containing the status, list of post summaries, and username
 */
@GetMapping("/posts-summary")
public ResponseEntity<Map<String, Object>> getAllPostsSummary(){
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NotFoundException("Please Log in"));
    Map<String, Object> response = new LinkedHashMap<>();
    List<PostSummary> posts = postService.getAllPostSummaries(user.getId());

    response.put("status", true);
    response.put("result", posts);
    return ResponseEntity.ok(response);
}
/**
 * Retrieves paginated post summaries with pagination details.
 *
 * @param page the page number (default is 0)
 * @param size the number of items per page (default is 5)
 * @return ResponseEntity containing the status, paginated post summaries, and pagination info
 */
@GetMapping("/posts/paginated")
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
/**
 * Retrieves a single post by its ID.
 *
 * @param post_id the ID of the post to retrieve
 * @return ResponseEntity containing the status and the post data if found
 * @throws NotFoundException if the post with the given ID does not exist
 */
@GetMapping("/posts/{post_id}")
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
/**
 * Deletes a post by its ID if the user is authorized.
 *
 * @param post_id the ID of the post to delete
 * @return ResponseEntity containing the status and a success message if deletion is successful
 * @throws NotFoundException if the post is not found or the user is not authorized to delete it
 */
@DeleteMapping("/posts/{post_id}")
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
/**
 * Toggles the like status for a post (like or unlike).
 *
 * @param post_id the ID of the post to like or unlike
 * @return ResponseEntity containing the status and message indicating whether the post was liked or unliked
 * @throws NotFoundException if the user is not logged in or the post does not exist
 */
@PostMapping("/posts/like/{post_id}")
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
/**
 * Toggles the bookmark status for a post (bookmark or remove from bookmarks).
 *
 * @param post_id the ID of the post to bookmark or remove from bookmarks
 * @return ResponseEntity containing the status and message indicating whether the post was bookmarked or removed
 * @throws NotFoundException if the user is not logged in or the post does not exist
 */
@PostMapping("/posts/bookmarks/{post_id}")
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
/**
 * Retrieves posts associated with a specific topic by its ID.
 *
 * @param topic_id the ID of the topic to retrieve posts for
 * @return ResponseEntity containing the status and the list of posts for the given topic
 * @throws NotFoundException if no posts are found for the given topic
 */
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
/**
 * Retrieves posts created by the logged-in user.
 *
 * @return ResponseEntity containing the status and the list of posts created by the user
 * @throws NotFoundException if the user is not logged in
 */
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
/**
 * Retrieves posts created by a specific user identified by their username.
 *
 * @param username the username of the user whose posts are to be retrieved
 * @return ResponseEntity containing the status and the list of posts for the given user
 */
@GetMapping("/posts/user/{username}")
public ResponseEntity<Map<String, Object>> getPostsByUser(@PathVariable String username){
    List<PostSummary> posts = postService.getPostsByUsername(username);
    Map<String, Object> response = new LinkedHashMap<>();
    response.put("status", true);
    response.put("result", posts);
    return ResponseEntity.ok(response);
}
/**
 * Retrieves all likes for a specific post by its ID.
 *
 * @param post_id the ID of the post to retrieve likes for
 * @return ResponseEntity containing the status and the list of likes for the given post
 * @throws NotFoundException if the post does not exist
 */
@GetMapping("/posts/likes/{post_id}")
public ResponseEntity<Map<String, Object>> getPostLikes(@PathVariable Long post_id){
    Map<String, Object> response = new LinkedHashMap<>();
    Post post = postService.getPostById(post_id);
    if (post == null) {
        throw new NotFoundException("Post not found");
    }
    List<LikesPayload> likes = likeService.getAllLikesOfPost(post_id);
    response.put("status", true);
    response.put("result", likes);
    return ResponseEntity.ok(response);
}
/**
 * Retrieves all bookmarks for a specific post by its ID.
 *
 * @param post_id the ID of the post to retrieve bookmarks for
 * @return ResponseEntity containing the status and the list of bookmarks for the given post
 * @throws NotFoundException if the post does not exist
 */
@GetMapping("/posts/bookmarks/{post_id}")
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
/**
 * Retrieves posts bookmarked by a specific user identified by their user ID.
 *
 * @param user_id the ID of the user to retrieve bookmarked posts for
 * @return ResponseEntity containing the status and the list of bookmarked posts for the given user
 */
@GetMapping("/posts/bookmarks/{user_id}/user")
public ResponseEntity<Map<String, Object>> getPostBookMarksByUser(@PathVariable Long user_id){
    Map<String, Object> response = new LinkedHashMap<>();
    List<PostSummary> bookMarks = postService.getPostByBookMarksId(user_id);
    response.put("status", true);
    response.put("result", bookMarks);
    return ResponseEntity.ok(response);
}
/**
 * Searches for posts based on the provided search term and returns matching posts.
 *
 * @param searchTerm the term to search for in post titles
 * @return ResponseEntity containing the status and the list of posts that match the search term
 * @throws NotFoundException if the user is not logged in
 */
@GetMapping("/posts/search")
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
/**
 * Updates an existing post by its ID. Optionally, a new image file can be uploaded along with the post's updated details.
 *
 * @param postId the ID of the post to be updated
 * @param post the updated post details
 * @param file the new image file to be uploaded (optional)
 * @return ResponseEntity containing the status and a message or updated post details
 * @throws NotFoundException if the user is not found or the post does not exist
 * @throws UnauthorizedException if the logged-in user is not the owner of the post
 */
@PutMapping(value = "/posts/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<Map<String, Object>> updatePost(@PathVariable Long postId, @RequestPart Post post, @RequestPart(value = "file", required = false)  MultipartFile file) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
    Post existingPost = postService.getPostById(postId);
    Map<String, Object> response = new LinkedHashMap<>();

    if(file != null && !file.isEmpty()) {
        String filePath = s3FileStorageUtils.uploadProfilePic(file);
        existingPost.setImgUrl(filePath);
    }

    if(!Objects.equals(user.getId(), existingPost.getUser().getId())){
        response.put("status", false);
        response.put("message", "You are not authorized to update this profile");
    } else {
        if(post.getTitle()!= null) existingPost.setTitle(post.getTitle());
        if(post.getContent() != null) existingPost.setContent(post.getContent());
        Post updatedPost = postService.savePost(user, existingPost);
        response.put("status", true);
        response.put("result", "Post Updated successfully");
    }
    return ResponseEntity.ok(response);
}

}
