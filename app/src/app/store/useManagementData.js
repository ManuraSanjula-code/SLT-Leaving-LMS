import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchManagementData, fetchPaginatedUsers } from '../store/managementSlice';

export const useManagementData = () => {
  const dispatch = useDispatch();
  const { data, loading, error } = useSelector(state => state.management);

  useEffect(() => {
    if (!data) {
      dispatch(fetchManagementData());
    }
  }, [dispatch, data]);

  const refetch = () => {
    dispatch(fetchManagementData());
  };

  return { data, loading, error, refetch };
};

export const usePaginatedUsers = (page, limit) => {
  const dispatch = useDispatch();
  const { paginatedUsers } = useSelector(state => state.management);

  useEffect(() => {
    dispatch(fetchPaginatedUsers({ page, limit }));
  }, [dispatch, page, limit]);

  return { paginatedUsers };
};