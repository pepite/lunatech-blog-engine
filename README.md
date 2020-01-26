# lunatech-blog-engine
The blog engine that powers the future blog.lunatech.com

This blog engine simply goes to a github repository and expect a repository with the following structure: 

In the `posts` directory a file with the following structure `yyyy-MM-dd-name-of-the-blog.adoc`

In the `media` directory a directory `yyyy-MM-dd-name-of-the-blog` with at least a `background.png` file and all other assets files needed for the blog post.

## Configuration of the application
```
accessToken = your-secret-token
github_organisation = lunatech-labs
github_repository = lunatech-blog
github_branch = master
# The main background image
background = "https://lunatech.cdn.prismic.io/lunatech/c01fd6de48c3cdb8bda7247b0b94b84b14f3a488_kevin-horvat-1354011-unsplash.jpg"
```

You can modify the template in the `app/views/` directory.

## Starting the application 

`sbt run` and go in your browser to `http:localhost:9000`


