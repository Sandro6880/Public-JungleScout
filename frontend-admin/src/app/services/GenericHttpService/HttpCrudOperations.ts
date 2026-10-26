import {Observable} from 'rxjs';

export interface HttpCrudOperations<T, ID> {
    get(id: ID): Observable<T>;
    getAll(): Observable<T[]>;
    save(t: T): Observable<T>;
    update(id: ID, t: T): Observable<T>;
    delete(id: ID): Observable<any>;
}
