FROM python:alpine AS copy
RUN mkdir -p /build
ADD tech /build/tech
ADD build.py /build
ADD requirements.txt /build
WORKDIR /build
RUN pip install -r requirements.txt
RUN python build.py

FROM jekyll/jekyll:stable
COPY --from=copy /build/tech .
RUN bundle
RUN jekyll build

CMD ["jekyll", "serve"]
