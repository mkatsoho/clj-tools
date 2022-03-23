(ns clj-ping.core
  (:gen-class)
  (:require [clojure.java.shell :as sh]))

;; Refer to Javadoc(http://docs.oracle.com/javase/7/docs/api/java/net/InetAddress.html#isReachable(int)
;; (defn java-ping
;;   "Time an .isReachable ping to a given domain"
;;   [domain timeoutSeconds]
;;   (let [addr (java.net.InetAddress/getByName domain)
;;         start (. System (nanoTime))
;;         result (.isReachable addr (* 1000 timeoutSeconds))
;;         total (/ (double (- (. System (nanoTime)) start)) 1000000.0)]
;;     {:host domain
;;      :result result
;;      :time total}))


(defn getNanoTime []
  (. System (nanoTime)))

(defn funcElapsedMs
  "exec the f(args) and return as a map, plus key :elapsedMs"
  [f & args]
  (let [start (getNanoTime)
        rtn (apply f args)  ;; TODO, like (f args)
        end (getNanoTime)
        elapsedMs (/ (double (- end start)) 1000000.0)]
    (if (= (type {}) (type rtn))
      (assoc rtn :elapsedMs elapsedMs)
      {:rtn rtn :elapsedMs elapsedMs})))

(defn shell "exec bash shell for cmd" [cmd]
  (sh/sh "bash" "-c" cmd))

(defn nslookup "get the IP of the given host" [host]
  (.getHostAddress (java.net.InetAddress/getByName host)))


(defn shell-ping
  "using bash to call ping for a given domain"
  [host timeoutSeconds]
  (let [ip (nslookup host)
        cmd (str "ping -c 1 -W " timeoutSeconds " " ip)
        rtn (funcElapsedMs shell cmd)]
    (merge {:host host
            :timeoutSeconds timeoutSeconds
            :ip ip
            :cmd cmd
            :status (if (= 0 (:exit rtn)) "pass" "failed")}
           rtn)))




(defn showUsage []
  (println "Usage: lein run www.baidu.com 10   # ping www.abc.com with timeout 10s\n"))


(defn -main "main function" [& args]
  (let [withoutParms (if (nil? (first args)) true false)
        host (if-let [url0 (first args)] url0 "www.baidu.com")
        timeoutSeconds (if-let [timeout_sec (first (rest args))] (Integer/parseInt timeout_sec) 10)
        rtn (shell-ping host timeoutSeconds)
        ;; rtn (shell "ping -c 1 -W 10 182.61.200.7")
        ;; rtn (java-ping host timeoutSeconds)
        ]
    (println "Ping:" (:host rtn) "| Status:" (:status rtn) "| Cmd:" (:cmd rtn) "| Returns:" (:exit rtn) "| Elapsed(ms):" (:elapsedMs rtn) "| Err:" (:err rtn) "\n")
    ;; (when withoutParms (println "DEBUG:" rtn))
    (when withoutParms (showUsage))
    (System/exit 0) ;; work around, "bash -c" hangs, "refer to https://clojuredocs.org/clojure.java.shell/sh#notes
    ))
