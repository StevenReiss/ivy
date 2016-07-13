/************************************************************************/
/*									*/
/*		ivy_pthread.H						*/
/*									*/
/*	Interface to Posix threads					*/
/*									*/
/************************************************************************/
/*	Copyright 2005 Brown University -- Steven P. Reiss			*/
/*********************************************************************************
 *  Copyright 2005, Brown University, Providence, RI.				 *
 *										 *
 *			  All Rights Reserved					 *
 *										 *
 *  Permission to use, copy, modify, and distribute this software and its	 *
 *  documentation for any purpose other than its incorporation into a		 *
 *  commercial product is hereby granted without fee, provided that the 	 *
 *  above copyright notice appear in all copies and that both that		 *
 *  copyright notice and this permission notice appear in supporting		 *
 *  documentation, and that the name of Brown University not be used in 	 *
 *  advertising or publicity pertaining to distribution of the software 	 *
 *  without specific, written prior permission. 				 *
 *										 *
 *  BROWN UNIVERSITY DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS		 *
 *  SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND		 *
 *  FITNESS FOR ANY PARTICULAR PURPOSE.  IN NO EVENT SHALL BROWN UNIVERSITY	 *
 *  BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY 	 *
 *  DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS,		 *
 *  WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS		 *
 *  ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 	 *
 *  OF THIS SOFTWARE.								 *
 *										 *
 ********************************************************************************/


/* RCS: $Header: /pro/spr_cvs/pro/ivy/stdlib/src/ivy_pthread.H,v 1.4 2013/11/08 01:32:18 spr Exp $ */

/************************************************************************
 *
 * $Log: ivy_pthread.H,v $
 * Revision 1.4  2013/11/08 01:32:18  spr
 * Updates for newer g++ on mac.
 *
 * Revision 1.3  2011-01-11 00:27:15  spr
 * Fixups for lock finding code.
 *
 * Revision 1.2  2006/06/21 02:19:07  spr
 * Formatting.
 *
 * Revision 1.1  2005/06/28 17:22:11  spr
 * Move stdlib from bloom to ivy for use in veld, etc.
 *
 *
 ************************************************************************/


#ifndef IVY_PTHREAD_ALREADY_INCLUDED
#define IVY_PTHREAD_ALREADY_INCLUDED


#ifndef _REENTRANT
#define _REENTRANT
#endif

#include <pthread.h>
#include <semaphore.h>
#include <signal.h>
#include <ivy_datatypes.H>



namespace Ivy {


/************************************************************************/
/*									*/
/*	Constants							*/
/*									*/
/************************************************************************/


const	pthread_t	IVY_INVALID_THREAD_ID = 0;
const	pthread_key_t	IVY_INVALID_THREAD_KEY = 0;
const	unsigned int	IVY_INVLAID_LOCK_TYPE = 0xffffffff;




/************************************************************************/
/*									*/
/*	Default thraad creation routines				*/
/*									*/
/************************************************************************/

extern "C" {
   inline void *  IVY__default_creation_rtn(void *);
   inline void	  IVY__default_key_destructor(void *);
}



/************************************************************************/
/*									*/
/*	IvyThread -- basic thread structure				*/
/*									*/
/************************************************************************/


class IvyThread {

private:
   pthread_t thread_id;

/************************************************************************/
/*									*/
/*	Creation methods						*/
/*									*/
/************************************************************************/

public:
   IvyThread()			{ thread_id = IVY_INVALID_THREAD_ID; }
   IvyThread(pthread_t t)	{ thread_id = t; }
   virtual ~IvyThread() 	{ }

   void self()				{ thread_id = pthread_self(); }

   int create(size_t stack_size = 0,void * stack_base = NULL) {
      pthread_attr_t attr;
      thread_id = IVY_INVALID_THREAD_ID;
      pthread_attr_init(&attr);
#ifdef __USE_XOPEN2K
      if (stack_size != 0 || stack_base != NULL)
	 pthread_attr_setstack(&attr,stack_base,stack_size);
#else
      if (stack_size != 0) pthread_attr_setstacksize(&attr,stack_size);
      if (stack_base != NULL) pthread_attr_setstackaddr(&attr,stack_base);
#endif
      return pthread_create(&thread_id,&attr,IVY__default_creation_rtn,this);
    }


/************************************************************************/
/*									*/
/*	Basic thread operations 					*/
/*									*/
/************************************************************************/

public:
   int isValid() const {
      if (thread_id == IVY_INVALID_THREAD_ID) return FALSE;
      if (pthread_kill(thread_id,0) != 0) return FALSE;
      return TRUE;
    }

   pthread_t getThreadId() const    { return thread_id; }
   operator pthread_t() const	 { return thread_id; }

   int waitFor(void ** status_ptr = NULL) {
      if (thread_id == IVY_INVALID_THREAD_ID) return 0;
      int sts = pthread_join(thread_id,status_ptr);
      if (sts == 0) thread_id = IVY_INVALID_THREAD_ID;
      return sts;
    }

   int kill(int sig = 15)	{ return pthread_kill(thread_id,sig); }

   int getPriority() {
      int pol;
      struct sched_param param;
      int sts = pthread_getschedparam(thread_id,&pol,&param);
      if (sts != 0) return -1;
      return param.sched_priority;
    }

   int priority(int pri) {
      int pol;
      struct sched_param param;
      int sts = pthread_getschedparam(thread_id,&pol,&param);
      if (sts != 0) {
	 param.sched_priority = pri;
	 sts = pthread_setschedparam(thread_id,pol,&param);
       }
      return sts;
    }

   int detach() 		{ return pthread_detach(thread_id); }

   int cancel() 		{ return pthread_cancel(thread_id); }

   int operator ==(const IvyThread& t) const
      { return pthread_equal(thread_id,t.thread_id); }
   int operator !=(const IvyThread& t) const { return ! operator == (t); }



/************************************************************************/
/*									*/
/*	User overridable operations					*/
/*									*/
/************************************************************************/

   virtual void * run() 				{ return NULL; }




/************************************************************************/
/*									*/
/*	Functions that apply to the current thread only 		*/
/*									*/
/************************************************************************/

   static void exit(void * rslt)	{ pthread_exit(rslt); }

   int sigsetmask(int how,const sigset_t * set,sigset_t * oset) {
      return pthread_sigmask(how,set,oset);
    }


/************************************************************************/
/*									*/
/*	Functions that apply to all threads in general			*/
/*									*/
/************************************************************************/

   static int getConcurrency()		{ return pthread_getconcurrency(); }
   static int setConcurrency(int d)	{ return pthread_setconcurrency(d); }
   static int incrConcurrency(int delta = 1)
      { return pthread_setconcurrency(delta+pthread_getconcurrency()); }

   static void testCancel()		{ pthread_testcancel(); }

};




/************************************************************************/
/*									*/
/*	IvyThreadKey -- key for thread-specific data			*/
/*									*/
/************************************************************************/


class IvyGenericKeyData {
public:
  virtual ~IvyGenericKeyData()			{ }

};



template<class D>
class IvyThreadKey {

private:
   class KeyData : public IvyGenericKeyData {
   private:
      IvyThreadKey * for_key;
      D user_data;
   public:
      KeyData(IvyThreadKey * k,D ud) {
	 for_key = k;
	 user_data = ud;
       }
      ~KeyData() {
	 for_key->destroy(user_data);
       }
      IvyThreadKey * getThreadKey()		{ return for_key; }
      D getUserData()				{ return user_data; }
    };

private:
   pthread_key_t thread_key;


   //
   //	Constructors and creation methods for IvyThreadKey
   //
public:
   IvyThreadKey()			{ thread_key = IVY_INVALID_THREAD_KEY; }
   IvyThreadKey(pthread_key_t k)	{ thread_key = k; }
   virtual ~IvyThreadKey()		{ }

   int create() {
      thread_key = IVY_INVALID_THREAD_KEY;
      return pthread_key_create(&thread_key,IVY__default_key_destructor);
    }



   //
   //	Methods for destroying the key and individual values
   //

   int remove() {
      int sts = pthread_key_delete(thread_key);
      if (sts == 0) thread_key = IVY_INVALID_THREAD_KEY;
      return sts;
    }

   virtual void destroy(D)		{ }	// called for user data when
						// thread is destroyed



   //
   //	Access methods for IvyThreadKey
   //

   int operator ==(const IvyThreadKey& k) const {
      return thread_key == k.thread_key;
    }
   int operator !=(const IvyThreadKey& k) const      { return ! operator ==(k); }

   pthread_key_t getThreadKey() const	{ return thread_key; }
   operator pthread_key_t() const	{ return thread_key; }



   //
   //	Methods for setting data
   //

   void setData(D value) {
      if (thread_key == IVY_INVALID_THREAD_KEY) create();
      KeyData * kd = new KeyData(this,value);
      pthread_setspecific(thread_key,kd);
    }

   D getData() {
      if (thread_key == IVY_INVALID_THREAD_KEY) return NULL;
      KeyData * kd = (KeyData *) pthread_getspecific(thread_key);
      if (kd == NULL) return NULL;
      return kd->getUserData();
    }

   friend void IVY__default_key_destructor(void *);
};




/************************************************************************/
/*									*/
/*	IvyMutex -- mutual exclusion lock				*/
/*									*/
/************************************************************************/


class IvyMutex {

private:
   pthread_mutex_t mutex_lock;

public:
   IvyMutex()				{ }
   IvyMutex(const pthread_mutex_t& m)	{ mutex_lock = m; }

   int create(bool process_share = false) {
      pthread_mutexattr_t attr;
      pthread_mutexattr_init(&attr);
      pthread_mutexattr_setpshared(&attr,
	(process_share ? PTHREAD_PROCESS_SHARED : PTHREAD_PROCESS_PRIVATE));
      return pthread_mutex_init(&mutex_lock,&attr);
    }

   int createThread()			{ return create(false); }
   int createProcess()			{ return create(true); }

   int destroy()			{ return pthread_mutex_destroy(&mutex_lock); }

   pthread_mutex_t& getMutex()			{ return mutex_lock; }
   operator pthread_mutex_t *() volatile	{ return (pthread_mutex_t *) &mutex_lock; }

   int lock() volatile
      { return pthread_mutex_lock((pthread_mutex_t *) &mutex_lock); }
   int tryLock() volatile
      { return pthread_mutex_trylock((pthread_mutex_t *) &mutex_lock); }
   int unlock() volatile
      { return pthread_mutex_unlock((pthread_mutex_t *) &mutex_lock); }
};



/************************************************************************/
/*									*/
/*	IvyRwLock -- read/write lock					*/
/*									*/
/************************************************************************/


#ifndef NO_RWLOCKS

class IvyRwLock {

private:
   pthread_rwlock_t rw_lock;

public:
   IvyRwLock()				{ }
   IvyRwLock(const pthread_rwlock_t r)	{ rw_lock = r; }

   int create(bool process_share = false) {
      pthread_rwlockattr_t attr;
      pthread_rwlockattr_init(&attr);
      pthread_rwlockattr_setpshared(&attr,
	(process_share ? PTHREAD_PROCESS_SHARED : PTHREAD_PROCESS_PRIVATE));
      return pthread_rwlock_init(&rw_lock,&attr);
    }

   int createThread()			{ return create(false); }
   int createProcess()			{ return create(true); }

   int destroy()			{ return pthread_rwlock_destroy(&rw_lock); }

   pthread_rwlock_t& getRwLock()	{ return rw_lock; }
   operator pthread_rwlock_t *()	{ return &rw_lock; }

   int readLock()		{ return pthread_rwlock_rdlock(&rw_lock); }
   int writeLock()		{ return pthread_rwlock_wrlock(&rw_lock); }
   int unlock() 		{ return pthread_rwlock_unlock(&rw_lock); }
   int tryReadLock()		{ return pthread_rwlock_tryrdlock(&rw_lock); }
   int tryWriteLock()		{ return pthread_rwlock_trywrlock(&rw_lock); }
};

#endif


/************************************************************************/
/*									*/
/*	IvyCondition -- condition variables				*/
/*									*/
/************************************************************************/


class IvyCondition {

private:
   pthread_cond_t cond_lock;

public:
   IvyCondition()				{ }
   IvyCondition(const pthread_cond_t& c)	{ cond_lock = c; }

   int create(bool process_share = false) {
      pthread_condattr_t attr;
      pthread_condattr_init(&attr);
      pthread_condattr_setpshared(&attr,
	(process_share ? PTHREAD_PROCESS_SHARED : PTHREAD_PROCESS_PRIVATE));
      return pthread_cond_init(&cond_lock,&attr);
    }

   int createThread()		{ return create(false); }
   int createProcess()		{ return create(true); }

   int destroy()		{ return pthread_cond_destroy(&cond_lock); }

   pthread_cond_t& getCondition()	{ return cond_lock; }
   operator pthread_cond_t *()		{ return &cond_lock; }

   int wait(volatile IvyMutex& mtx) volatile
      { return pthread_cond_wait((pthread_cond_t *) &cond_lock,mtx); }
   int timedWait(IvyMutex& mtx,const struct timespec * abstime) volatile {
      return pthread_cond_timedwait((pthread_cond_t *) &cond_lock,mtx,abstime);
    }
   int timedWait(IvyMutex& mtx,int millisec) {
      struct timespec ts;
      ts.tv_sec = millisec/1000;
      ts.tv_nsec = (millisec%1000)*1000000;
      return timedWait(mtx,&ts);
    }

   int signal() volatile
      { return pthread_cond_signal((pthread_cond_t *) &cond_lock); }
   int broadcast() volatile
      { return pthread_cond_broadcast((pthread_cond_t *) &cond_lock); }
};




/************************************************************************/
/*									*/
/*	IvySemaphore -- semaphore					*/
/*									*/
/************************************************************************/


#ifndef NO_SEMAS


class IvySemaphore {

private:
   sem_t sem_data;

public:
   IvySemaphore()			{ }
   IvySemaphore(const sem_t& s) { sem_data = s; }

   int create(unsigned int count  = 0,bool process_share = false) {
      return sem_init(&sem_data,process_share,count);
    }
   int createThread(unsigned int count = 0)	{ return create(count,false); }
   int createProcess(unsigned int count = 0)	{ return create(count,true); }

   int destroy()		{ return sem_destroy(&sem_data); }

   sem_t& getSemaphore()	{ return sem_data; }
   operator sem_t * ()		{ return &sem_data; }

   int wait() volatile		{ return sem_wait((sem_t *) &sem_data); }
   int P()			{ return wait(); }
   int tryWait() volatile	{ return sem_trywait((sem_t *) &sem_data); }
   int post() volatile		{ return sem_post((sem_t *) &sem_data); }
   int V()			{ return post(); }

   int count() const
      { int ct = 0; sem_getvalue((sem_t *) &sem_data,&ct); return ct; }
};



#else

class IvySemaphore {

private:
   IvyMutex data_mutex;
   IvyCondition block_cond;
   int sema_count;

public:
   IvySemaphore()			{ }

   int create(unsigned int count = 0,bool process_share = false) {
      sema_count = count;
      data_mutex.create(process_share);
      block_cond.create(process_share);
      return 0;
    }
   int createThread(unsigned int count = 0)	{ return create(count,false); }
   int createProcess(unsigned int count = 0)	{ return create(count,true); }

   int destroy() {
      data_mutex.destroy();
      block_cond.destroy();
      return 0;
    }

   int wait() volatile {
      data_mutex.lock();
      for ( ; ; ) {
	 if (sema_count > 0) {
	    --sema_count;
	    break;
	  }
	 else {
	    block_cond.wait(data_mutex);
	  }
       }
      data_mutex.unlock();
      return 0;
    }

   int tryWait() volatile {
      int sts = 0;
      data_mutex.lock();
      if (sema_count > 0) --sema_count;
      else sts = -1;
      data_mutex.unlock();
      return sts;
    }

   int post() volatile {
      data_mutex.lock();
      sema_count += 1;
      if (sema_count == 1) block_cond.signal();
      data_mutex.unlock();
      return 0;
    }

   int P()			{ return wait(); }
   int V()			{ return post(); }

   int count() const		{ return sema_count; }
};

#endif




/************************************************************************/
/*									*/
/*	Local routines							*/
/*									*/
/************************************************************************/


inline void *
IVY__default_creation_rtn(void * ud)
{
   IvyThread * ft = (IvyThread *) ud;
   return ft->run();
}



inline void
IVY__default_key_destructor(void * vd)
{
   IvyGenericKeyData * kd = (IvyGenericKeyData *) vd;
   if (kd != NULL) delete kd;
}



}	// end of namespace Ivy


#endif


/* end of ivy_pthread.H */
