import requests

def copy_files():
    tree = requests.get("https://api.github.com/repos/lunatech-labs/lunatech-blog/git/trees/f2a3bd4398e3c1e6fe3f90a13a48ad51a2562160")
    github_files = [item['path'] for item in tree.json()["tree"]]
    for github_file in github_files:
        raw_file = requests.get('https://raw.githubusercontent.com/lunatech-labs/lunatech-blog/master/posts' + github_file)
        with open('tech/_posts/' + github_file, 'w') as local_file:
            local_file.write(raw_file.text)

if __name__ == "__main__":
    copy_files()
