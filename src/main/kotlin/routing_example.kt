import org.aquiles.*
import org.aquiles.core.HttpResponse
import org.aquiles.core.HttpStatus
import org.aquiles.core.jsonResponse
data class BlogPost(
    val id: Int?,
    val title: String,
    val body: String,
)
val samplePosts = mutableListOf(
    BlogPost(
        id = 1,
        title = "First Blog Post",
        body = "This is the content of my first blog post. It's exciting to start sharing my thoughts!",
    ),
    BlogPost(
        id = 2,
        title = "Exploring QAPI",
        body = "In this post, I'll dive into the features and benefits of the QAPI framework for Kotlin.",

        ),
    BlogPost(
        id = 3,
        title = "My Favorite Books",
        body = "Sharing a list of my all-time favorite books and why I recommend them.",
    )
)
class MyPostsScope : RoutingScope() { // A dedicated scope for post-related routes


    init {
        addScopeMiddleware(myFilter)
    }

    @Get("/")
    fun getAllPosts(): HttpResponse {
        return HttpResponse.Ok().jsonResponse(samplePosts)
    }

    @Post("/add")
    fun createPost(newPost : BlogPost): HttpResponse {
        val newId = samplePosts.maxOfOrNull { it.id!! }?.plus(1) ?: 1
        val postToAdd = newPost.copy(id = newId)
        samplePosts.add(postToAdd)
        return HttpResponse.Ok().jsonResponse(postToAdd)
    }

    @Get("/{id}")
    fun getPostById(id : Int): HttpResponse {
        val post = samplePosts.find { it.id == id }
        println("hey hey ${id}")
        return if (post != null) {
            HttpResponse.Ok().jsonResponse(post)
        } else {
            HttpResponse(HttpStatus.NOT_FOUND, "Post not found")
        }
    }

    @Put("/{id}")
    fun updatePost(id : Int,updatedPost: BlogPost): HttpResponse {

        val index = samplePosts.indexOfFirst { it.id == id }
        return if (index != -1) {
            samplePosts[index] = updatedPost.copy(id = id)
            HttpResponse.Ok().jsonResponse(updatedPost)
        } else {
            HttpResponse(HttpStatus.NOT_FOUND, "Post not found")
        }
    }

    @Delete("/{id}")
    fun deletePost(id : Int): HttpResponse {
        val removed = samplePosts.removeIf { it.id == id }
        return if (removed) {
            HttpResponse(HttpStatus.OK, content = "Deleted successfully")
        } else {
            HttpResponse(HttpStatus.NOT_FOUND, "Post not found")
        }
    }


    @Post("/upload", multipartFiles = ["file"])
    fun upload(file: UploadFile): String {

        file.write("./upload/")


        return "http://localhost:9000/download/${file.fileName}"
    }
}
