# lunatech-blog-engine
The blog engine that powers the future blog.lunatech.com

This blog engine goes to the [lunatech-blog](https://github.com/lunatech-labs/lunatech-blog) GitHub repository and expects a repository with the following structure: 

- a `posts` directory containing files with the following structure `yyyy-MM-dd-name-of-the-blog.adoc`
- a `media` directory with the corresponding subdirectory `yyyy-MM-dd-name-of-the-blog` with at least a `background.png` file and all other assets files needed for the blog post.

## To run locally

1. Install [Ruby and Jekyll](https://jekyllrb.com/docs/installation/)
2. Install Python 3
3. Create a virtual environment: `python3 -m venv venv`
4. Get inside it: `. venv/bin/activate`
5. Install the requirements: `pip install -r requirements.txt`
6. Prepare Jekyll: `python build.py`
7. Run Jekyll: `cd tech && bundle exec jekyll serve`
8. Visit [localhost:4000](localhost:4000)

`bundle exec jekyll serve`

## Configuration of the application


