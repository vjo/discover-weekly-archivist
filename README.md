# discover-weekly-archivist

Archieve Spotify's Discover Weekly playlist in a new playlist.

## Disclamer

This project is build in [Clojure](http://clojure.org) without any experience, just for fun.

It uses a few libraries:
* [clj-spotify](https://github.com/blmstrm/clj-spotify)
* [tools.cli](https://github.com/clojure/tools.cli)

### Limitation

Your `Discover Weekly` playlist should be in your 50 first playlists. (Pull request welcome)

## Installation
```shell
$ git clone https://github.com/vjo/discover-weekly-archivist.git
$ cd discover-weekly-archivist
```

You need to set your `user_id` (spotify login) and API `token` in `src/discover_weekly_archivist/core.clj`.

Then to compile:
```shell
$ lein uberjar
```

## Usage

```shell
$ java -jar target/uberjar/discover-weekly-archivist-0.1.0-SNAPSHOT-standalone.jar -p
```

## Options

```shell
$ java -jar target/uberjar/discover-weekly-archivist-0.1.0-SNAPSHOT-standalone.jar --help
discover-weekly-archivist will create a new Spotify playlist with the content of your "Discover Weekly" playlist.

Usage: discover-weekly-archivist [options]

Options:
  -n, --name NAME  Optional: name of the new playlist.
  -p, --public     Optional: make the new playlist public.
  -h, --help       Display help.

```

## TODO

- [ ] oAuth?

## License

TODO
