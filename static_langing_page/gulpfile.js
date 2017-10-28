var gulp         = require('gulp');
var browserSync  = require('browser-sync').create();
var sass         = require('gulp-sass');
var autoprefixer = require('gulp-autoprefixer');

var browserify   = require('browserify');
var source       = require('vinyl-source-stream');
var streamify    = require('gulp-streamify');
var babel        = require('gulp-babel');

var del          = require('del');
var gutil        = require('gulp-util');
var uglify       = require('gulp-uglify');

var imagemin     = require('gulp-imagemin');

gulp.task('serve', ['sass', 'browserify', 'imagemin', 'js'], function() {

    browserSync.init({
        server: "./"
    });

    gulp.watch("src/scss/*.scss", ['sass']);
    gulp.watch("src/js/*.js", ['browserify']);
    gulp.watch("src/img/**/*", ['imagemin']);
    gulp.watch("./*.html").on('change', browserSync.reload);
});

gulp.task('sass', function() {
    return gulp.src("src/scss/main.scss")
        .pipe(sass())
        .on('error', gutil.log)
        .pipe(autoprefixer({ browsers: ['last 3 versions'], cascade: false }))
        .pipe(gulp.dest("dest/css"))
        .pipe(browserSync.stream());
});

gulp.task('browserify', function() {
    browserify('src/js/main.js')
        .bundle()
        .pipe(source('app.js'))
        .pipe(streamify(babel({ presets: ['es2015'] })))
        .pipe(gulp.dest('dest/js'))
        .pipe(browserSync.stream());
})

gulp.task('js', function() {
  gulp.src(['src/js/*', '!src/js/main.js', '!src/js/lib'])
      .pipe(uglify())
      .on('error', gutil.log)
      .pipe(gulp.dest('dest/js'))
})

gulp.task('imagemin', function() {
  gulp.src('src/img/**/*')
      .pipe(imagemin())
      .pipe(gulp.dest('dest/img'))
})

gulp.task('cleanUp', function() {
  del.sync('dest/*')
})

gulp.task('default', ['cleanUp', 'serve']);
