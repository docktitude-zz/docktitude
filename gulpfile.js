var gulp = require('gulp');
var uglify = require('gulp-uglify');

gulp.task('scripts', function() {
    gulp.src('./build/**/*.js')
        .pipe(uglify())
        .pipe(gulp.dest(__dirname))
});

gulp.task('default', ['scripts']);
