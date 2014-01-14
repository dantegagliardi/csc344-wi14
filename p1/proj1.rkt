#lang racket

(require rsound)
(define (samples sec) (* 44100 sec))

(define fname "rope.wav")
(define snd (rs-read/clip fname (samples 30) (samples 60)))
(define song-len (rs-frames snd))

(random-seed 48852)

(define (songr clip-count)
  (begin
    (define len (add1 (random 2)))
    (define rand-sound (random (- song-len (samples 1))))
    (cond [(eq? clip-count 0)
           (clip snd rand-sound (round (+ rand-sound (/ (samples 1) len))))]
          [else
           (define clip1 (clip snd rand-sound (round (+ rand-sound (/ (samples 1) len)))))
           (rs-append clip1 (songr (sub1 clip-count)))])))

(define song
  (songr 30))

(rs-write song "ropeNew.wav")